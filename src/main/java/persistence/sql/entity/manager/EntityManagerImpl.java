package persistence.sql.entity.manager;

import jdbc.JdbcTemplate;
import persistence.sql.dml.exception.NotFoundIdException;
import persistence.sql.dml.query.builder.DeleteQueryBuilder;
import persistence.sql.dml.query.builder.InsertQueryBuilder;
import persistence.sql.dml.query.builder.SelectQueryBuilder;
import persistence.sql.dml.repository.RepositoryMapper;
import persistence.sql.entity.EntityMappingTable;
import persistence.sql.entity.model.Criteria;
import persistence.sql.entity.model.Criterias;
import persistence.sql.entity.model.DomainType;
import persistence.sql.entity.model.Operators;

import java.lang.reflect.Field;
import java.util.Collections;

public class EntityManagerImpl<T, K> implements EntityManger<T, K> {

    private final JdbcTemplate jdbcTemplate;

    public EntityManagerImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public T find(Class<T> clazz, K id) {
        final EntityMappingTable entityMappingTable = EntityMappingTable.from(clazz);
        final RepositoryMapper<T> repositoryMapper = new RepositoryMapper<>(clazz);
        final DomainType pkDomainType = entityMappingTable.getPkDomainTypes();

        Criteria criteria = new Criteria(pkDomainType.getColumnName(), id.toString(), Operators.EQUALS);
        Criterias criterias = new Criterias(Collections.singletonList(criteria));

        SelectQueryBuilder selectQueryBuilder = SelectQueryBuilder.of(entityMappingTable, criterias);
        return jdbcTemplate.queryForObject(selectQueryBuilder.toSql(), repositoryMapper::mapper);
    }

    @Override
    public void persist(T entity) {
        InsertQueryBuilder insertQueryBuilder = InsertQueryBuilder.from(entity);
        jdbcTemplate.execute(insertQueryBuilder.toSql());
    }

    @Override
    public void remove(T entity) {
        final EntityMappingTable entityMappingTable = EntityMappingTable.from(entity.getClass());
        final DomainType pkDomainType = entityMappingTable.getPkDomainTypes();

        Criteria criteria = new Criteria(pkDomainType.getColumnName(), getFieldValue(entity, entity.getClass(), pkDomainType.getName()), Operators.EQUALS);
        Criterias criterias = new Criterias(Collections.singletonList(criteria));

        DeleteQueryBuilder deleteQueryBuilder = DeleteQueryBuilder.of(entityMappingTable.getTableName(), criterias);
        jdbcTemplate.execute(deleteQueryBuilder.toSql());
    }

    private String getFieldValue(T entity, Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(entity).toString();
        } catch (Exception e) {
            throw new NotFoundIdException();
        }
    }

}
