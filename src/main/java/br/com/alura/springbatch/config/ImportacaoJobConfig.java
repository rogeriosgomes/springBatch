package br.com.alura.springbatch.config;

import br.com.alura.springbatch.model.Dados;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class ImportacaoJobConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public Job importacaoJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("importacao", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                     ItemReader<Dados> itemReader, ItemWriter<Dados> itemWriter) {

        return new StepBuilder("step", jobRepository)
                .<Dados,Dados>chunk(20, transactionManager)
                .reader(itemReader)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public ItemReader<Dados> itemReader() {

        BeanWrapperFieldSetMapper<Dados> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Dados.class);

        return new FlatFileItemReaderBuilder<Dados>()
                .name("dadosficticios.csv")
                .resource(new ClassPathResource("dadosficticios.csv"))
                .comments("--")
                .delimited()
                .delimiter(";")
                .names("nome","cpf","agencia","conta","valor","mesReferencia")
                .fieldSetMapper(fieldSetMapper)
                .build();

    }

    @Bean
    public ItemWriter<Dados> itemWriter() {

        return new JdbcBatchItemWriterBuilder<Dados>()
                .dataSource(dataSource)
                .sql("INSERT INTO dados"
                +"(nome, cpf, agencia, conta, valor, mes_Referencia)"
                +"values(:nome, :cpf, :agencia, :conta, :valor, :mesReferencia)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();

    }
}

