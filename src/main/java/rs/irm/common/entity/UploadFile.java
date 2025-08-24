package rs.irm.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.entity.AppUser;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="uploadfile",
	uniqueConstraints = @UniqueConstraint(name="uploadfile_uuid_unique", columnNames = { "uuid" }),
	indexes = @Index(columnList = "uuid",name = "uploadfile_uuid_index")
		)
public class UploadFile {

	@Id
	private Long id;
	
	@JoinColumn(name="appuser",nullable = false,foreignKey = @ForeignKey(name="fk_uploadfile_appuser"))
	private AppUser appUser;
	
	@Column(nullable = false)
	private String uuid;
	
	@Column(nullable = false)
	private byte[] bytes;

	@Column(name="currenttime",nullable = false)
	private LocalDateTime currentTime;
}
