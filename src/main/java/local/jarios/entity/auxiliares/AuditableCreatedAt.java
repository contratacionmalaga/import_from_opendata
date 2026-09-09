package local.jarios.entity.auxiliares;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import local.jarios.helpers.LocalDateTimeHelper;
import lombok.Getter;
import lombok.Setter;

/**
 * Description: Clase que añade elementos de Auditorías a las clases que la extienden Author: juan
 * Date: 04/06/2024 Team: Juan Antonio
 */
@Setter
@Getter
@MappedSuperclass
public class AuditableCreatedAt {

  @JsonIgnore
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @JsonIgnore
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTimeHelper.getLocalDateTimeNow();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTimeHelper.getLocalDateTimeNow();
  }
}
