package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * {@link UserRepository} port 的 JPA 實作，負責 {@code User}（domain）與 {@code UserJpaEntity}（persistence）互轉。
 */
@Repository
class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public void save(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getId(), user.getUsername(), user.getDisplayName(), user.getPassword());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    private User toDomain(UserJpaEntity entity) {
        return User.reconstruct(entity.getId(), entity.getUsername(), entity.getDisplayName(), entity.getPassword());
    }
}
