package smu.poodle.smnavi.user.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import smu.poodle.smnavi.user.auth.Authority;

import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    String email;

    @Column(unique = true)
    String nickname;

    String password;

    @Enumerated(EnumType.STRING)
    Authority authority;

    @OneToOne(mappedBy = "user")
    JwtRefreshToken jwtRefreshToken;

    public Collection<GrantedAuthority> getGrantedAuthority() {
        return Collections.singleton(new SimpleGrantedAuthority(authority.toString()));
    }

}