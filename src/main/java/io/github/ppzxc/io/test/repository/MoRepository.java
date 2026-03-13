package io.github.ppzxc.io.test.repository;

import io.github.ppzxc.io.test.entity.Mo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoRepository extends JpaRepository<Mo, Long> {
}
