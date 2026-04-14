package com.alvarezg.expenses;

import com.alvarezg.expenses.model.Category;
import com.alvarezg.expenses.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    Category.builder().name("Comida").icon("🍔").color("#FF5733").build(),
                    Category.builder().name("Transporte").icon("🚌").color("#3380FF").build(),
                    Category.builder().name("Ocio").icon("🎮").color("#9B33FF").build(),
                    Category.builder().name("Salud").icon("💊").color("#33FF57").build(),
                    Category.builder().name("Educación").icon("📚").color("#FFD700").build(),
                    Category.builder().name("Hogar").icon("🏠").color("#FF8C00").build(),
                    Category.builder().name("Otros").icon("📦").color("#808080").build()
            ));
            System.out.println("✅ Categorías iniciales creadas");
        }
    }
}