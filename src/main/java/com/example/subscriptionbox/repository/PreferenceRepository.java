package com.example.subscriptionbox.repository;
import com.example.subscriptionbox.model.Preference; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface PreferenceRepository extends JpaRepository<Preference,Long>{ Optional<Preference> findByUserId(Long id); }