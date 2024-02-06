package com.jvbarbosa.dscommerce.factories;

import com.jvbarbosa.dscommerce.entities.Category;
import com.jvbarbosa.dscommerce.entities.Product;

public class ProductFactory {
    public static Product createProduct() {
        Category category = CategoryFactory.createCategory();
        Product product = new Product(
                1L,
                "Console PlayStation 5",
                "Jogue como nunca antes. Curta uma nova geração de jogos incríveis do PlayStation",
                4269.90,
                "playstation-5.png"
        );
        product.getCategories().add(category);

        return product;
    }

    public static Product createProduct(String name) {
        Product product = createProduct();
        product.setName(name);
        return product;
    }
}
