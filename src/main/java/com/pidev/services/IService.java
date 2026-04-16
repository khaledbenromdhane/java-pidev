package com.pidev.services;

import java.util.List;

public interface IService<T> {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(int id);
    List<T> afficher();
    T afficherParId(int id);
}
