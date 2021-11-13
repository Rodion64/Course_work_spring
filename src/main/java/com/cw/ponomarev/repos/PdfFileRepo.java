package com.cw.ponomarev.repos;

import com.cw.ponomarev.model.PdfFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PdfFileRepo extends CrudRepository<PdfFile, Long> {
    Optional<PdfFile> findById(Long id);
}
