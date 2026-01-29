package local.jarios.entity.auxiliares;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import local.jarios.helpers.LocalDateTimeHelper;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Description: Clase que añade elementos de Auditorías a las clases que la extienden Author: juan
 * Date: 04/06/2024 Team: Juan Antonio
 */
@Setter
@Getter
@MappedSuperclass
public class AuditableCreatedAt {

  //Getters y Setters
  @JsonIgnore
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {

    //
    createdAt = LocalDateTimeHelper.getLocalDateTimeNow();
  }
}
