package com.example.social_media.service.implement;

import com.example.social_media.dto.NationDto;
import com.example.social_media.model.entity.Nation;
import com.example.social_media.repository.mysql.NationRepository;
import com.example.social_media.service.NationService;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NationServiceImpl implements NationService {

    private final NationRepository nationRepository;

    public NationServiceImpl(NationRepository nationRepository) {
        this.nationRepository = nationRepository;
    }

    @Override
    public List<NationDto> getAllNations() {
        return nationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private NationDto convertToDto(Nation nation) {
        return new NationDto(
            nation.getNationId(),
            nation.getName()
        );
    }
}
