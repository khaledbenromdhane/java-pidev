    package com.pidev.services;

    import java.util.List;
    import java.util.Optional;

    public interface CrudService<T, ID> {
        T create(T entity);

        T update(T entity);

        boolean deleteById(ID id);

        Optional<T> findById(ID id);

        List<T> findAll();
    }
