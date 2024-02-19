package persistence.sql.ddl.dialect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.entity.Person1;
import persistence.sql.ddl.dialect.h2.H2Dialect;

import static org.assertj.core.api.Assertions.assertThat;

class DialectTest {

    @DisplayName("Person 만드는 테이블 쿼리문을 반환한다.")
    @Test
    void getCreateTableSql() {
        Dialect dialect = new H2Dialect();

        String sql = dialect.createTable(Person1.class);
        final String expected = "CREATE TABLE Person1(\n" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                "name VARCHAR,\n" +
                "age INTEGER\n" +
                ");";

        assertThat(sql).isEqualTo(expected);
    }
    
}