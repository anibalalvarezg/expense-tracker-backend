package com.alvarezg.expenses.scheduler;

import com.alvarezg.expenses.model.Budget;
import com.alvarezg.expenses.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j  // ← genera un logger automáticamente
public class BudgetScheduler {

    private final BudgetRepository budgetRepository;

    // Se ejecuta el día 1 de cada mes a las 00:00
    @Scheduled(cron = "0 0 0 1 * *")
//    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void createMonthlyBudgets() {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        // Calcular el mes anterior
        LocalDate previousMonth = today.minusMonths(1);
        int prevMonth = previousMonth.getMonthValue();
        int prevYear = previousMonth.getYear();

        log.info("🗓️ Iniciando creación automática de budgets para {}/{}",
                currentMonth, currentYear);

        // Traer todos los budgets del mes anterior
        List<Budget> previousBudgets = budgetRepository
                .findAllByMonthAndYear(prevMonth, prevYear);

        if (previousBudgets.isEmpty()) {
            log.info("⚠️ No hay budgets del mes anterior para copiar");
            return;
        }

        int created = 0;
        int skipped = 0;

        for (Budget previous : previousBudgets) {
            // Verificar que no exista ya para este mes
            boolean alreadyExists = budgetRepository
                    .existsByUserAndMonthAndYearAndCategoryIsNull(
                            previous.getUser(), currentMonth, currentYear
                    );

            if (alreadyExists) {
                log.info("⏭️ Ya existe budget para usuario {} en {}/{}",
                        previous.getUser().getEmail(), currentMonth, currentYear);
                skipped++;
                continue;
            }

            // Crear el nuevo budget copiando el monto del mes anterior
            Budget newBudget = Budget.builder()
                    .user(previous.getUser())
                    .amount(previous.getAmount())
                    .month(currentMonth)
                    .year(currentYear)
                    .category(null)
                    .build();

            budgetRepository.save(newBudget);
            created++;

            log.info("✅ Budget creado para usuario {} → ${} para {}/{}",
                    previous.getUser().getEmail(),
                    newBudget.getAmount(),
                    currentMonth,
                    currentYear);
        }

        log.info("🏁 Proceso finalizado → {} creados, {} omitidos", created, skipped);
    }
}