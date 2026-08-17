package local.jarios.services;

import java.util.List;
import java.util.Set;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;

public record ImportPersistencePlan(
    Log miLog,
    Configuracion configuracion,
    List<String> nifList,
    List<OrganoContratacion> organoContratacionList,
    Set<Feed> feedSet,
    List<Historico> historicoList,
    Estadistica estadistica) {}
