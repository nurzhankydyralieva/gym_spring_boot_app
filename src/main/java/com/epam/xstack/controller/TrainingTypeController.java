package com.epam.xstack.controller;

import com.epam.xstack.aspects.training_type.annotations.SaveTrainingTypeEndPointAspectAnnotation;
import com.epam.xstack.aspects.training_type.annotations.SelectAllTrainingTypeEndPointAspectAnnotation;
import com.epam.xstack.exceptions.validator.NotNullValidation;
import com.epam.xstack.models.dto.training_type_dto.TrainingTypeDTO;
import com.epam.xstack.service.training_type_service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/training_types")
public class TrainingTypeController {
    private final TrainingTypeService trainingTypeService;
    private final NotNullValidation validation;

    @Operation(summary = "Save Training Type to database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Training type saved successfully"),
                    @ApiResponse(responseCode = "401", description = "Bad credentials")})
    @SaveTrainingTypeEndPointAspectAnnotation
    @PostMapping("/save")
    public ResponseEntity<TrainingTypeDTO> save(@Valid @RequestBody TrainingTypeDTO trainingTypeDTO, BindingResult result) {
        validation.nullValidation(result);
        return new ResponseEntity<>(trainingTypeService.save(trainingTypeDTO), HttpStatus.OK);
    }

    @Operation(summary = "Get all Trainings",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Training type selected successfully"),
                    @ApiResponse(responseCode = "401", description = "Bad credentials")})
    @SelectAllTrainingTypeEndPointAspectAnnotation
    @GetMapping("/all")
    public ResponseEntity<List<TrainingTypeDTO>> findAll() {
        return new ResponseEntity<>(trainingTypeService.findAll(), HttpStatus.FOUND);
    }
}