package persistence.sql.ddl.dialect.database;

import persistence.sql.ddl.dialect.Dialect;
import persistence.sql.ddl.dialect.DialectResolution;
import persistence.sql.ddl.dialect.exception.NotFoundDatabase;
import persistence.sql.ddl.dialect.h2.H2Dialect;

import java.util.Arrays;
import java.util.function.Supplier;

public enum Database {

    H2(H2Dialect::new) {
        @Override
        public boolean productNameMatchers(String databaseName) {
            return this.name().equals(databaseName);
        }
    };

    private final Supplier<Dialect> dialectSupplier;

    public abstract boolean productNameMatchers(String databaseName);


    Database(Supplier<Dialect> dialectSupplier) {
        this.dialectSupplier = dialectSupplier;
    }

    public Supplier<Dialect> getDialectSupplier() {
        return dialectSupplier;
    }

    public static Database from(final DialectResolution dialectResolution) {
        return Arrays.stream(values())
                .filter(value -> value.productNameMatchers(dialectResolution.getDatabaseName()))
                .findFirst()
                .orElseThrow(NotFoundDatabase::new);
    }

}
