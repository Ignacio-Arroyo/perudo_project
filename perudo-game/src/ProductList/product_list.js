// src/components/ProductList.js
import React, { useState, useEffect } from 'react';
import { getProducts, addProduct } from '../services/ProductService';

const ProductList = () => {
  const [products, setProducts] = useState([]);
  const [newProduct, setNewProduct] = useState({
    name: '',
    description: '',
    price: 0,
    stock: 0
  });

  useEffect(() => {
    const fetchProducts = async () => {
      const products = await getProducts();
      setProducts(products);
    };
    fetchProducts();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNewProduct({
      ...newProduct,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const product = await addProduct(newProduct);
    setProducts([...products, product]);
    setNewProduct({
      name: '',
      description: '',
      price: 0,
      stock: 0
    });
  };

  return (
    <div>
      <h1>Products</h1>
      <ul>
        {products.map(product => (
          <li key={product.id}>
            {product.name} - {product.description} - ${product.price} - Stock: {product.stock}
          </li>
        ))}
      </ul>
      <h2>Add a New Product</h2>
      <form onSubmit={handleSubmit}>
        <input type="text" name="name" placeholder="Name" value={newProduct.name} onChange={handleChange} />
        <input type="text" name="description" placeholder="Description" value={newProduct.description} onChange={handleChange} />
        <input type="number" name="price" placeholder="Price" value={newProduct.price} onChange={handleChange} />
        <input type="number" name="stock" placeholder="Stock" value={newProduct.stock} onChange={handleChange} />
        <button type="submit">Add Product</button>
      </form>
    </div>
  );
};

export default ProductList;
