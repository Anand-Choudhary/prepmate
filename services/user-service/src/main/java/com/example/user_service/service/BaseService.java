package com.example.user_service.service;

import java.util.Optional;

public interface BaseService<T,I>
{
    Optional<T> findById(I id);

    Iterable<T> findAll();

    T create(T t);

    T update(T t);
}
