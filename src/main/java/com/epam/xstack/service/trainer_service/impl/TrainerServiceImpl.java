package com.epam.xstack.service.trainer_service.impl;

import com.epam.xstack.aspects.trainer_aspects.authentication_aspects.annotations.*;
import com.epam.xstack.configuration.jwt_config.JwtService;
import com.epam.xstack.exceptions.exception.UserIdNotFoundException;
import com.epam.xstack.exceptions.exception.UserNameNotExistsException;
import com.epam.xstack.exceptions.generator.PasswordUserNameGenerator;
import com.epam.xstack.exceptions.validator.ActivationValidator;
import com.epam.xstack.exceptions.validator.UserNameExistenceValidator;
import com.epam.xstack.mapper.trainee_mapper.TraineeMapper;
import com.epam.xstack.mapper.trainer_mapper.*;
import com.epam.xstack.mapper.training_mapper.TrainingListMapper;
import com.epam.xstack.models.dto.trainer_dto.request.*;
import com.epam.xstack.models.dto.trainer_dto.response.*;
import com.epam.xstack.models.entity.Trainer;
import com.epam.xstack.models.enums.Code;
import com.epam.xstack.models.enums.Role;
import com.epam.xstack.repository.TrainerRepository;
import com.epam.xstack.service.trainer_service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {
    private final TrainerRepository trainerRepository;
    private final PasswordUserNameGenerator generator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserNameExistenceValidator checkUserNameExistence;
    private final TrainerProfileSelectRequestMapper getTrainerProfileRequestMapper;
    private final TrainerProfileUpdateRequestMapper updateTrainerProfileRequestMapper;
    private final TrainerActivateDeActivateMapper activateDeActivateTrainerMapper;
    private final TrainerTrainingsListMapper trainerTrainingsListMapper;
    private final ActivationValidator checkActivation;

    @Override
    @SelectTrainerTrainingsListAspectAnnotation
    public TrainerTrainingsListResponseDTO selectTrainerTrainingsList(UUID id, TrainerTrainingsListRequestDTO requestDTO) {
        Trainer trainerId = trainerRepository.findById(id).get();
        trainerTrainingsListMapper.toEntity(requestDTO);

        if (trainerId.getUsername().equals(requestDTO.getUserName())) {
            return TrainerTrainingsListResponseDTO
                    .builder()
                    .trainings(TrainingListMapper.INSTANCE.toDtos(trainerId.getTrainings()))
                    .build();
        } else {
            throw UserIdNotFoundException.builder()
                    .codeStatus(Code.USER_ID_NOT_FOUND)
                    .message("User id:  " + trainerId.getId() + " or user name: " + trainerId.getUsername() + " not correct.")
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }
    }

    @Override
    @ActivateDe_ActivateTrainerAspectAnnotation
    public TrainerOkResponseDTO activateDe_ActivateTrainer(UUID id, TrainerActivateDeActivateDTO dto) {
        Trainer trainer = activateDeActivateTrainerMapper.toEntity(dto);
        Trainer existingTrainer = trainerRepository.findById(id).get();

        checkActivation.checkActiveOrNotTrainerActive(id, dto);

        existingTrainer.setIsActive(dto.getIsActive());
        trainerRepository.save(existingTrainer);
        activateDeActivateTrainerMapper.toDto(trainer);
        return TrainerOkResponseDTO
                .builder()
                .code(Code.STATUS_200_OK)
                .response("Activate DeActivate Trainer updated")
                .build();
    }

    @Override
    @UpdateTrainerProfileAspectAnnotation
    public TrainerProfileUpdateResponseDTO updateTrainerProfile(UUID id, TrainerProfileUpdateRequestDTO requestDTO) {
        Trainer trainer = updateTrainerProfileRequestMapper.toEntity(requestDTO);
        Trainer trainerToBeUpdated = trainerRepository.findById(id).get();
        if (trainerToBeUpdated.getId() == id) {
            trainerToBeUpdated.setFirstName(requestDTO.getFirstName());
            trainerToBeUpdated.setLastName(requestDTO.getLastName());
            trainerToBeUpdated.setIsActive(requestDTO.getIsActive());

            trainerRepository.save(trainerToBeUpdated);
            updateTrainerProfileRequestMapper.toDto(trainer);
        }

        return TrainerProfileUpdateResponseDTO
                .builder()
                .userName(trainerToBeUpdated.getUsername())
                .firstName(trainerToBeUpdated.getFirstName())
                .lastName(trainerToBeUpdated.getLastName())
                .specialization(trainerToBeUpdated.getSpecialization())
                .isActive(trainerToBeUpdated.getIsActive())
                .trainees(TraineeMapper.INSTANCE.toDtos(trainerToBeUpdated.getTraineeList()))
                .build();
    }

    @Override
    @SelectTrainerProfileByUserNameAspectAnnotation
    public TrainerProfileSelectResponseDTO selectTrainerProfileByUserName(UUID id, TrainerProfileSelectRequestDTO requestDTO) {
        Trainer trainer = getTrainerProfileRequestMapper.toEntity(requestDTO);
        Trainer trainerId = trainerRepository.findById(id).get();

        if (trainerId.getUsername().equals(requestDTO.getUserName())) {
            getTrainerProfileRequestMapper.toDto(trainer);

            return TrainerProfileSelectResponseDTO
                    .builder()
                    .firstName(trainerId.getFirstName())
                    .lastName(trainerId.getLastName())
                    .specialization(trainerId.getSpecialization())
                    .isActive(trainerId.getIsActive())
                    .traineeList(TraineeMapper.INSTANCE.toDtos(trainerId.getTraineeList()))
                    .build();
        } else {
            throw UserNameNotExistsException.builder()
                    .codeStatus(Code.USER_NOT_FOUND)
                    .message("User with name - " + requestDTO.getUserName() + " not exists in database")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }


    @Override
    @SaveTraineeAspectAnnotation
    public TrainerRegistrationResponseDTO saveTrainer(TrainerRegistrationRequestDTO request) {
        String generatedPassword = generator.generateRandomPassword();
        String createdUserName = generator.generateUserName(request.getFirstName(), request.getLastName());

        var createTrainer = new Trainer();
        createTrainer.setUserName(createdUserName);
        createTrainer.setFirstName(request.getFirstName());
        createTrainer.setLastName(request.getLastName());
        createTrainer.setPassword(passwordEncoder.encode(generatedPassword));
        createTrainer.setIsActive(true);
        createTrainer.setRole(Role.TRAINER);

        checkUserNameExistence.userNameExists(createdUserName);

        trainerRepository.save(createTrainer);
        var jwtToken = jwtService.generateToken(createTrainer);
        return TrainerRegistrationResponseDTO
                .builder()
                .userName(createTrainer.getUsername())
                .token(jwtToken)
                .password(generatedPassword)
                .build();
    }
}
