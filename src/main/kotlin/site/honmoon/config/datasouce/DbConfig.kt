package site.honmoon.config.datasouce


import jakarta.persistence.EntityManagerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["site.honmoon"],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
@EnableJdbcRepositories(
    basePackages = ["site.honmoon.jdbc.*"]
)
class DatabaseConfig {

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    fun dataSource(): DataSource = DataSourceBuilder.create().build()

    @Primary
    @Bean
    fun entityManagerFactory(
        builder: EntityManagerFactoryBuilder,
        dataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(dataSource)
            .packages("site.honmoon.*")
            .build()
    }

    @Primary
    @Bean
    fun transactionManager(
        entityManagerFactory: EntityManagerFactory,
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}
