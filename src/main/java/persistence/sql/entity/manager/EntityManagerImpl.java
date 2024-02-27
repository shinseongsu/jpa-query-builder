package persistence.sql.entity.manager;

import jdbc.JdbcTemplate;
import persistence.sql.dml.query.builder.DeleteQueryBuilder;
import persistence.sql.dml.query.builder.InsertQueryBuilder;
import persistence.sql.dml.query.builder.SelectQueryBuilder;
import persistence.sql.entity.EntityMappingTable;
import persistence.sql.entity.conditional.Criteria;
import persistence.sql.entity.conditional.Criterion;
import persistence.sql.entity.model.DomainType;
import persistence.sql.entity.model.Operators;

import java.util.Collections;
import java.util.List;

public class EntityManagerImpl<T, K> implements EntityManger<T, K> {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManagerMapper<T> entityManagerMapper;

    public EntityManagerImpl(final JdbcTemplate jdbcTemplate,
                             Class<T> clazz) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityManagerMapper = new EntityManagerMapper<>(clazz);
    }


    @Override
    public List<T> findAll(final Class<T> clazz) {
        final EntityMappingTable entityMappingTable = EntityMappingTable.from(clazz);

        SelectQueryBuilder selectQueryBuilder = SelectQueryBuilder.from(entityMappingTable);
        return jdbcTemplate.query(selectQueryBuilder.toSql(), entityManagerMapper::mapper);
    }

    @Override
    public T find(final Class<T> clazz, final K id) {
        final EntityMappingTable entityMappingTable = EntityMappingTable.from(clazz);
        final DomainType pkDomainType = entityMappingTable.getPkDomainTypes();

        Criterion criterion = new Criterion(pkDomainType.getColumnName(), id.toString(), Operators.EQUALS);
        Criteria criteria = new Criteria(Collections.singletonList(criterion));

        SelectQueryBuilder selectQueryBuilder = SelectQueryBuilder.of(entityMappingTable, criteria);
        return jdbcTemplate.queryForObject(selectQueryBuilder.toSql(), entityManagerMapper::mapper);
    }

    @Override
    public void persist(final T entity) {
        InsertQueryBuilder insertQueryBuilder = InsertQueryBuilder.from(entity);
        jdbcTemplate.execute(insertQueryBuilder.toSql());
    }

    @Override
    public void remove(final T entity) {
        final EntityMappingTable entityMappingTable = EntityMappingTable.from(entity.getClass());
        final DomainType pkDomainType = entityMappingTable.getPkDomainTypes();

        Criterion criterion = new Criterion(pkDomainType.getColumnName(), entityManagerMapper.getFieldValue(entity, pkDomainType.getColumnName()), Operators.EQUALS);
        Criteria criteria = new Criteria(Collections.singletonList(criterion));

        DeleteQueryBuilder deleteQueryBuilder = DeleteQueryBuilder.of(entityMappingTable.getTableName(), criteria);
        jdbcTemplate.execute(deleteQueryBuilder.toSql());
    }

    @Override
    public void removeAll(final Class<T> clazz) {
        final EntityMappingTable entityMappingTable = EntityMappingTable.from(clazz);
        DeleteQueryBuilder deleteQueryBuilder = DeleteQueryBuilder.from(entityMappingTable.getTableName());

        jdbcTemplate.execute(deleteQueryBuilder.toSql());
    }
}
