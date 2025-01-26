package com.example.datageneratormicroservice.web.mapper;

import org.mapstruct.Mapper;

import java.util.List;

public interface Mappable<E, D> {

    E toEntity(D d);

    List<E> toEntity(List<D> d);

    D toDto(E e);

    List<D> toDto(List<E> e);

}
