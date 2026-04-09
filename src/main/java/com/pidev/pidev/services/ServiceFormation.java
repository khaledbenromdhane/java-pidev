package com.pidev.pidev.services;

import com.pidev.modeles.Formation;

import java.util.List;
import java.util.Optional;

public class ServiceFormation  implements CrudService<Formation> {
    @Override
    public Formation create(Object entity) {
        return null;
    }

    @Override
    public Formation update(Object entity) {
        return null;
    }

    @Override
    public boolean deleteById(Object o) {
        return false;
    }

    @Override
    public Optional<Formation> findById(Object o) {
        return Optional.empty();
    }

    @Override
    public List<Formation> findAll() {
        return List.of();
    }
}
