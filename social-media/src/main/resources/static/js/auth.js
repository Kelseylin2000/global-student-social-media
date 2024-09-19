function signIn(event) {
    event.preventDefault();
    const provider = "native";
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    fetch('/api/1.0/auth/signin', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ provider: provider, email: email, password: password })
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert('登入失敗: ' + data.error);
        } else if (data.data && data.data.access_token) {
            localStorage.setItem('access_token', data.data.access_token);
            localStorage.setItem('user_id', data.data.user.user_id);
            window.location.href = '/chatsession.html'; 
        } else {
            alert('登入失敗: 無法取得 access token。');
        }
    })
    .catch(error => console.error('錯誤:', error));
}

function signUp(event) {
    event.preventDefault();
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    fetch('/api/1.0/auth/signup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: name, email: email, password: password })
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert('註冊失敗: ' + data.error); 
        } else if (data.data && data.data.access_token) {
            localStorage.setItem('access_token', data.data.access_token);
            localStorage.setItem('user_id', data.data.user.user_id);
            window.location.href = '/chatsession.html';
        } else {
            alert('註冊失敗: 無法取得 access token。');
        }
    })
    .catch(error => console.error('錯誤:', error));
}