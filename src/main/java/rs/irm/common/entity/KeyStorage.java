package rs.irm.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name="keystorage")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KeyStorage {

	@Id
	private Long id;
	
	@Column(name="publickey",nullable = false)
	private byte[] publicKey;
	
	@Column(name="privatekey",nullable = false)
	private byte[] privateKey;
}
