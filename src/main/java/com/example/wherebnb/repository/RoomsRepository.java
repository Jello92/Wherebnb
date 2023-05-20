package com.example.wherebnb.repository;

import com.example.wherebnb.entity.Rooms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RoomsRepository extends JpaRepository<Rooms, Long> {

   Page<Rooms> findAll(Pageable pageable);

   List<Rooms> findAllByKeyword1OrKeyword2(String keyword1, String keyword2, Pageable pageable);

   List<Rooms> findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
           LocalDate checkInDate,
           LocalDate checkOutDate,
           Pageable pageable);


   List<Rooms> findByGuestNumGreaterThanEqualAndInfantExistAndPetExist(
           int guestNum,
           boolean infantExist,
           boolean petExist,
           Pageable pageable);



   List<Rooms>findAllByPeriodGreaterThanEqual(
           int period,
           Pageable pageable);

   List<Rooms>findByCheckInDateGreaterThanEqualAndCheckInDateLessThanEqual(
           LocalDate startOfMonth,
           LocalDate endOfMonth,
           Pageable pageable
   );
}
