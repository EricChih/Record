/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.provider.user;


import java.util.List;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * <p>內部使用者定義</p>
 *
 * @author ： user
 * @version :
 * @Date ： 2023/3/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@Component
@ConfigurationProperties(prefix = "hualiteq.security")
public class EmbeddedUserProperties {

    private List<EmbeddedUser> users;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @ToString(callSuper = true)
    @Builder
    public static class EmbeddedUser {

        private String username;

        private String password;

        private List<String> roles;

        @Builder.Default
        private boolean enabled = true;

        @Builder.Default
        private boolean accountLocked = false;

        @Builder.Default
        private boolean accountExpired = false;

        @Builder.Default
        private boolean credentialsExpired = false;

    }

}
