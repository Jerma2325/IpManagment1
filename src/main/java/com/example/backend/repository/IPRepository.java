package com.example.backend.repository;

import com.example.backend.model.IntellectualProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPRepository extends JpaRepository<IntellectualProperty, String> {

    List<IntellectualProperty> findByOwner(String owner);
}