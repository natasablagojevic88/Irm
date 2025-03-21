package rs.irm.administration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.enums.SmtpSecurity;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="smtpserver",
	uniqueConstraints = @UniqueConstraint(columnNames = { "name" },name = "smtpserver_name_unique")
		)
public class SmtpServer {

	@Id
	private Long id;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private String host;
	
	@Column(nullable = false)
	private Integer port;
	
	@Column(nullable = false)
	private Boolean authentication;
	
	@Column(nullable = false)
	private SmtpSecurity security;
	
	@Column(name="frommail",nullable = false)
	private String fromMail;
	
	
	@Column
	private String username;
	
	@Column
	private String password;
	
}
