var stompClient = null;
var currentChatId = null;
var userId = localStorage.getItem('user_id');
var currentFriendId = '';
var currentFriendName = '';
var accessToken = localStorage.getItem('access_token');
var userNameCache = {}; 

function connect() {
    var socket = new SockJS('/ws?token=' + accessToken);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/queue/messages', function(message) {
            showMessage(JSON.parse(message.body));
            loadSessions();
        });
    });
}

function getUserNameByUserId(userId, callback) {
    if (userNameCache[userId]) {
        callback(userNameCache[userId]);
    } else {
        axios.get(`/api/1.0/users/${userId}/username`)
        .then(function(response) {
            var userName = response.data.data;
            userNameCache[userId] = userName;
            callback(userName);
        })
        .catch(function(error) {
            console.error('Error loading user name:', error);
            callback('Unknown');
        });
    }
}

function loadSessions() {
    axios.get('/api/1.0/chat/sessions', {
        headers: { 'Authorization': 'Bearer ' + accessToken }
    })
    .then(function(response) {
        var sessions = response.data.data;
        var sessionList = document.getElementById('sessionList');
        sessionList.innerHTML = '';

        var userNamePromises = sessions.map(function(session) {
            var friendId = session.participants.find(id => id != userId);
            return new Promise(function(resolve, reject) {
                getUserNameByUserId(friendId, function(userName) {
                    resolve({ session: session, userName: userName });
                });
            });
        });

        Promise.all(userNamePromises).then(function(results) {
            results.forEach(function(result) {
                var session = result.session;
                var userName = result.userName;
                var latestMessageDate = new Date(session.latestMessage.createdAt).toLocaleDateString('zh-TW');
                var latestMessageTime = new Date(session.latestMessage.createdAt).toLocaleTimeString('zh-TW', {
                    hour: '2-digit',
                    minute: '2-digit'
                });

                var sessionElement = document.createElement('div');
                sessionElement.className = 'sessionItem';
                sessionElement.innerHTML = `
                    <div class="sessionDetailsContainer">
                        <div class="sessionDetails">
                            <div class="participantId">${userName}</div>
                            <div class="messageTime">${latestMessageDate} ${latestMessageTime}</div>
                        </div>
                        <div class="latestMessage">${session.latestMessage.content}</div>
                    </div>
                `;

                sessionElement.onclick = function() {
                    openChat(session.chatId, session.participants.find(id => id != userId));
                };

                sessionList.appendChild(sessionElement);
            });
        }).catch(function(error) {
            console.error('Error loading user names:', error);
        });
    })
    .catch(function(error) {
        console.error('Error loading sessions:', error);
    });
}

function openChat(chatId, friendId) {
    currentChatId = chatId;
    currentFriendId = friendId;
    currentFriendName = userNameCache[friendId];

    document.getElementById('sessionList').style.display = 'none';
    document.getElementById('chat').style.display = 'flex';
    document.getElementById('chatHeader').innerHTML = `
        <button id="backButton" onclick="backToSessions()">←</button>
        與 ${currentFriendName} 聊天
    `;
    document.getElementById('messageArea').innerHTML = '';

    axios.get('/api/1.0/chat/history', {
        params: { chatId: chatId, page: 0, size: 30 },
        headers: { 'Authorization': 'Bearer ' + accessToken }
    })
    .then(function(response) {
        var messages = response.data.data;
        messages.reverse();
        messages.forEach(function(message) {
            showMessage(message);
        });
    })
    .catch(function(error) {
        console.error('Error loading chat history:', error);
    });
}

function backToSessions() {
    document.getElementById('sessionList').style.display = 'flex';
    document.getElementById('chat').style.display = 'none';
    loadSessions();
}

function sendMessage() {
    var senderId = userId;
    var receiverId = currentFriendId;
    var content = document.getElementById('messageInput').value;
    if (content && stompClient && receiverId) {
        var chatId = senderId < receiverId ? (senderId + '_' + receiverId) : (receiverId + '_' + senderId);
        var message = {
            chatId: currentChatId || chatId,
            senderId: senderId,
            receiverId: receiverId,
            content: content
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
        document.getElementById('messageInput').value = '';
        showMessage(message);
    }
}

function showMessage(message) {
    if (message.chatId !== currentChatId) {
        return;
    }
    var messageArea = document.getElementById('messageArea');

    var messageBlock = document.createElement('div');
    messageBlock.className = 'messageBlock';

    var messageElement = document.createElement('div');
    messageElement.className = 'messageItem';

    if (message.senderId == userId) {
        messageElement.classList.add('sentMessage');
        messageBlock.style.justifyContent = 'flex-end';
    } else {
        messageElement.classList.add('receivedMessage');
        messageBlock.style.justifyContent = 'flex-start';
    }

    var displayName = message.senderId == userId ? '你' : currentFriendName;
    messageElement.innerHTML = `<b>${displayName}:</b> ${message.content}`;

    messageBlock.appendChild(messageElement);
    messageArea.appendChild(messageBlock);
    messageArea.scrollTop = messageArea.scrollHeight;
}

connect();
loadSessions();