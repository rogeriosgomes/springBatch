package br.com.alura.springbatch.repository;

import br.com.alura.springbatch.model.dados;
import org.springframework.data.jpa.repository.JpaRepository;

public interface dadosRepository extends JpaRepository<dados,Long> {
}
