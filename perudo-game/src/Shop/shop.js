// src/Shop/shop.js

import React, { useState, useEffect } from 'react';
import { getProducts } from '../services/ProductService'; // Corrected import path

const Shop = () => {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    const fetchProducts = async () => {
      const products = await getProducts();
      setProducts(products);
    };
    fetchProducts();
  }, []);

  return (
    <div>
      <h1>Shop</h1>
      <ul>
        {products.map(product => (
          <li key={product.id}>
            {product.name} - {product.description} - ${product.price}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Shop;
