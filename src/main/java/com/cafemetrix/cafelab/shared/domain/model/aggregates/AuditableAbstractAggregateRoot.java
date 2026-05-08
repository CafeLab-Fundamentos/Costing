package com.cafemetrix.cafelab.shared.domain.model.aggregates;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * Abstract base class for auditable aggregate roots.
 * Provides common auditing fields like ID, createdAt, and updatedAt.
 * Extends AbstractAggregateRoot to support domain events.
 *
 * @param <T> the type of the aggregate root
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AuditableAbstractAggregateRoot<T extends AbstractAggregateRoot<T>> extends AbstractAggregateRoot<T> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;

    public Long getId() { return id; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    /**
     * Register a domain event.
     * @param event the domain event to register
     */
    public void addDomainEvent(Object event) {
        super.registerEvent(event);
    }
}
