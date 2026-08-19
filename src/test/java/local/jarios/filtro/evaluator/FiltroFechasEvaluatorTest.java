package local.jarios.filtro.evaluator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import org.junit.jupiter.api.Test;

class FiltroFechasEvaluatorTest {

  private final FiltroFechasEvaluator evaluator = new FiltroFechasEvaluator();

  @Test
  void returnsTrueForEntryInsideNormalDateRange() {
    OpenDataExecutionContext context =
        contextWithRange(LocalDateTime.of(2026, 1, 31, 23, 59), LocalDateTime.of(2026, 1, 1, 0, 0));

    Entry entry = entryWithUpdated(LocalDateTime.of(2026, 1, 15, 12, 0));

    assertTrue(evaluator.evaluar(context, entry));
  }

  @Test
  void returnsTrueForEntryAtRangeBoundaries() {
    OpenDataExecutionContext context =
        contextWithRange(LocalDateTime.of(2026, 1, 31, 23, 59), LocalDateTime.of(2026, 1, 1, 0, 0));

    Entry entryAtStart = entryWithUpdated(LocalDateTime.of(2026, 1, 1, 0, 0));
    Entry entryAtEnd = entryWithUpdated(LocalDateTime.of(2026, 1, 31, 23, 59));

    assertTrue(evaluator.evaluar(context, entryAtStart));
    assertTrue(evaluator.evaluar(context, entryAtEnd));
  }

  @Test
  void returnsFalseForEntryOutsideNormalDateRange() {
    OpenDataExecutionContext context =
        contextWithRange(LocalDateTime.of(2026, 1, 31, 23, 59), LocalDateTime.of(2026, 1, 1, 0, 0));

    Entry entryBeforeStart = entryWithUpdated(LocalDateTime.of(2025, 12, 31, 23, 59));
    Entry entryAfterEnd = entryWithUpdated(LocalDateTime.of(2026, 2, 1, 0, 0));

    assertFalse(evaluator.evaluar(context, entryBeforeStart));
    assertFalse(evaluator.evaluar(context, entryAfterEnd));
  }

  @Test
  void returnsFalseForAscendingRange() {
    OpenDataExecutionContext context =
        contextWithRange(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 31, 23, 59));

    Entry entry = entryWithUpdated(LocalDateTime.of(2026, 1, 15, 12, 0));

    assertFalse(evaluator.evaluar(context, entry));
  }

  @Test
  void returnsFalseWhenEntryDateIsNull() {
    OpenDataExecutionContext context =
        contextWithRange(LocalDateTime.of(2026, 1, 31, 23, 59), LocalDateTime.of(2026, 1, 1, 0, 0));

    Entry entry = new Entry();

    assertFalse(evaluator.evaluar(context, entry));
  }

  private static OpenDataExecutionContext contextWithRange(
      LocalDateTime fechaInicio, LocalDateTime fechaFin) {
    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setFiltroFechaInicial(fechaInicio);
    context.setFiltroFechaFinal(fechaFin);
    return context;
  }

  private static Entry entryWithUpdated(LocalDateTime updated) {
    Entry entry = new Entry();
    entry.setUpdated(updated);
    return entry;
  }
}
