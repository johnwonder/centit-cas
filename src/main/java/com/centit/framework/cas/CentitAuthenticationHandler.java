package com.centit.framework.cas;

import java.security.GeneralSecurityException;
import java.util.Map;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;

import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.security.Md5Encoder;

@Component("centitAuthenticationHandler")
public class CentitAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    private static final String DEFAULT_PASSWORD_FIELD = "password";
    private static final String DEFAULT_SALT_FIELD = "salt";

    /**
     * The Sql statement to execute.
     */
    @NotNull
    protected String sql;

    /**
     * The Password field name.
     */
    @NotNull
    protected String passwordFieldName = DEFAULT_PASSWORD_FIELD;

    /**
     * The Salt field name.
     */
    @NotNull
    protected String saltFieldName = DEFAULT_SALT_FIELD;

  
    /**
     * Instantiates a new Query and encode database authentication handler.
     *
     * @param datasource The database datasource
     * @param sql the sql query to execute which must include a parameter placeholder
     *            for the user id. (i.e. {@code SELECT * FROM table WHERE username = ?}
     * @param algorithmName the algorithm name (i.e. {@code MessageDigestAlgorithms.SHA_512})
     */
    @Autowired(required = false)
    public CentitAuthenticationHandler(@Qualifier("queryEncodeDatabaseDataSource")
                                                           final DataSource datasource,
                                                       @Value("${cas.jdbc.authn.query.encode.sql:}")
                                                       final String sql) {
        super();
        //setName("密码{用户代码}认证服务器");
        if (datasource != null) {
            setDataSource(datasource);            
        }
        this.sql = sql;
    }

    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential)
            throws GeneralSecurityException, PreventedException {

        if (StringUtils.isBlank(this.sql) || getJdbcTemplate() == null) {
            throw new GeneralSecurityException("Authentication handler is not configured correctly");
        }

        final String username = getPrincipalNameTransformer().transform(transformedCredential.getUsername());
        final String encodedPsw = this.getPasswordEncoder().encode(transformedCredential.getPassword());

        try {
            final Map<String, Object> values = getJdbcTemplate().queryForMap(this.sql, username);
            final String digestedPassword = Md5Encoder.encodePassword(encodedPsw,
            		StringBaseOpt.objectToString(values.get(saltFieldName)));

            if (!values.get(this.passwordFieldName).equals(digestedPassword)) {
                throw new FailedLoginException("Password does not match value on record.");
            }
            return createHandlerResult(transformedCredential,
                    this.principalFactory.createPrincipal(username,values), null);

        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            } else {
                throw new FailedLoginException("Multiple records found for " + username);
            }
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }

    }

    /**
     * Sets password field name. Default is {@link #DEFAULT_PASSWORD_FIELD}.
     *
     * @param passwordFieldName the password field name
     */
    @Autowired
    public final void setPasswordFieldName(@Value("${cas.jdbc.authn.query.encode.password:" + DEFAULT_PASSWORD_FIELD + '}')
                                               final String passwordFieldName) {
        this.passwordFieldName = passwordFieldName;
    }

    /**
     * Sets salt field name. Default is {@link #DEFAULT_SALT_FIELD}.
     *
     * @param saltFieldName the password field name
     */
    @Autowired
    public final void setSaltFieldName(@Value("${cas.jdbc.authn.query.encode.salt:" + DEFAULT_SALT_FIELD + '}')
                                       final String saltFieldName) {
        this.saltFieldName = saltFieldName;
    }

}
