package kr.nanoit.mot.tester.repository;

import kr.nanoit.mot.tester.entity.Mo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoRepository extends JpaRepository<Mo, Long> {
}
