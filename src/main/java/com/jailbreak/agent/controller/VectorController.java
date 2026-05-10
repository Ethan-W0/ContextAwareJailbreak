package com.jailbreak.agent.controller;

import com.jailbreak.agent.model.AttackVector;
import com.jailbreak.agent.vector.AttackVectorRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vectors")
public class VectorController {

    private final AttackVectorRepository vectorRepository;

    public VectorController(AttackVectorRepository vectorRepository) {
        this.vectorRepository = vectorRepository;
    }

    @GetMapping
    public List<AttackVector> getVectors(@RequestParam(required = false) String category) {
        return vectorRepository.findByCategoryAndEnabled(Optional.ofNullable(category), true);
    }
}
