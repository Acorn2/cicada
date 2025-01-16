package top.crossoverjie.cicada.server.action.cookie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Function: cookie
 *
 * @author crossoverJie
 *         Date: 2018/12/4 18:56
 * @since JDK 1.8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpCookie {

    private String name;
    private String value;
    private String path;
    private String domain;
    private long maxAge;
    private boolean secure;
    private boolean httpOnly;
    private String sameSite;

}
