package com.zosh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.zosh.model.ListModel;

@Transactional(readOnly = true)
public interface ListRepository extends JpaRepository<ListModel, Long>{
//	@Query("SELECT l FROM ListModel l WHERE l.privateMode=false ORDER BY l.createdAt DESC")
//	List<ListModel> findPublicOrderByCreatedAtDesc();
//	
//	@Query("SELECT l from ListModel l JOIN l.user u WHERE u.id = :userId AND l.privateMode=true ORDER BY l.createdAt DESC")
//	List<ListModel> findPrivateOrderByCreatedAtDesc(Long userId);
	
	@Query("SELECT l from ListModel l JOIN l.user u WHERE l.privateMode=false OR u.id= :userId ORDER BY l.createdAt DESC")
	public List<ListModel> findAllOrderByCreatedAtDesc(Long userId);

}
