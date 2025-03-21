package rs.irm.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.entity.AppUser;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification")
public class Notification {

	@Id
	private Long id;

	@JoinColumn(name = "appuser", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_appuser"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private AppUser appUser;

	@Column(nullable = false)
	private String name;

	@Column
	private String body;

	@Column(nullable = false)
	private Boolean unread = true;

	@Column(name = "createationtime", nullable = false)
	private LocalDateTime createationTime = LocalDateTime.now();

	@Column(name = "readtime")
	private LocalDateTime readTime;
}
