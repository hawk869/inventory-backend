package com.danielesteban.inventory.services;

import com.danielesteban.inventory.dao.ICategoryDao;
import com.danielesteban.inventory.dao.IProductDao;
import com.danielesteban.inventory.model.Category;
import com.danielesteban.inventory.model.Product;
import com.danielesteban.inventory.response.ProductResponseRest;
import com.danielesteban.inventory.util.Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service @Transactional
public class ProductServiceImpl implements IProductService {

    private final ICategoryDao categoryDao;
    private final IProductDao productDao;

    public ProductServiceImpl(ICategoryDao categoryDao, IProductDao productDao) {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    @Override
    public ResponseEntity<ProductResponseRest> save(Product product, Long categoryId) {
        ProductResponseRest responseRest = new ProductResponseRest();
        List<Product> productList = new ArrayList<>();

        try {
            Optional<Category> category = categoryDao.findById(categoryId);
            if (category.isPresent())
                product.setCategory(category.get());
            else{
                responseRest.setMetadata("respuesta nok","-1", "Categoria no encontrada asociada al producto");
                return new ResponseEntity<>(responseRest, HttpStatus.NOT_FOUND);
            }
            Product productSaved = productDao.save(product);
            productList.add(productSaved);
            responseRest.getProductResponse().setProducts(productList);
            responseRest.setMetadata("respuesta ok", "00", "Producto guardado");
        }
        catch (Exception e){
                e.getStackTrace();
                responseRest.setMetadata("respuesta nok", "-1", "Error al guardar producto");
                return new ResponseEntity<>(responseRest, HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>(responseRest, HttpStatus.CREATED);
    }

    @Override @Transactional(readOnly = true)
    public ResponseEntity<ProductResponseRest> searchById(Long id) {
        ProductResponseRest responseRest = new ProductResponseRest();
        List<Product> productList = new ArrayList<>();
        try {
            Optional<Product> product = productDao.findById(id);
            if (product.isPresent()){
                byte[] imageDecompressed = Util.decompressZLib(product.get().getPhoto());
                product.get().setPhoto(imageDecompressed);
                productList.add(product.get());
                responseRest.getProductResponse().setProducts(productList);
                responseRest.setMetadata("Respuesta ok", "00", "Producto encontrado");
            }
            else {
                responseRest.setMetadata("Respuesta nok", "-1", "Producto no encontrado");
                return new ResponseEntity<>(responseRest, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e){
            e.getStackTrace();
            responseRest.setMetadata("Respuesta nok", "-1", "Error al buscar producto");
            return new ResponseEntity<>(responseRest, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseRest, HttpStatus.OK);
    }

    @Override @Transactional(readOnly = true)
    public ResponseEntity<ProductResponseRest> searchByName(String name) {
        ProductResponseRest responseRest = new ProductResponseRest();
        List<Product> productList;
        List<Product> products = new ArrayList<>();
        try {
            productList = productDao.findByNameContainingIgnoreCase(name);
            if (!productList.isEmpty()){
                productList.forEach(p -> {
                    byte[] imageDecompressed = Util.decompressZLib(p.getPhoto());
                    p.setPhoto(imageDecompressed);
                    products.add(p);
                });
                responseRest.getProductResponse().setProducts(products);
                responseRest.setMetadata("Respuesta ok", "00", "Producto encontrado");
            }
            else {
                responseRest.setMetadata("Respuesta nok", "-1", "Producto no encontrado");
                return new ResponseEntity<>(responseRest, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e){
            e.getStackTrace();
            responseRest.setMetadata("Respuesta nok", "-1", "Error al buscar producto");
            return new ResponseEntity<>(responseRest, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseRest, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProductResponseRest> deleteProduct(Long id) {
        ProductResponseRest responseRest = new ProductResponseRest();
        try {
            productDao.deleteById(id);
            responseRest.setMetadata("Respuesta ok", "00", "Producto eliminado");
        }
        catch (Exception e){
            e.getStackTrace();
            responseRest.setMetadata("Respuesta nok", "-1", "Error al eliminar producto");
            return new ResponseEntity<>(responseRest, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseRest, HttpStatus.OK);
    }

    @Override @Transactional(readOnly = true)
    public ResponseEntity<ProductResponseRest> getAllProducts() {
        ProductResponseRest responseRest = new ProductResponseRest();
        List<Product> products = new ArrayList<>();
        List<Product> productList;
        try {
            productList = (List<Product>) productDao.findAll();
            if (productList.size() > 0){
                productList.forEach(p -> {
                    byte[] imageDecompressed = Util.decompressZLib(p.getPhoto());
                    p.setPhoto(imageDecompressed);
                    products.add(p);
                });
                responseRest.getProductResponse().setProducts(products);
                responseRest.setMetadata("Respuesta ok", "00", "Productos encontrados");
            } else {
                responseRest.setMetadata("Respuesta nok", "-1", "Productos no encontrados");
                return new ResponseEntity<>(responseRest, HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
            e.getStackTrace();
            responseRest.setMetadata("Respuesta nok", "-1", "Error al buscar productos");
            return new ResponseEntity<>(responseRest, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseRest, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProductResponseRest> updateProduct(Product product, Long categoryId, Long productId) {
        ProductResponseRest responseRest = new ProductResponseRest();
        List<Product> products = new ArrayList<>();
        try {
            Optional<Category> category = categoryDao.findById(categoryId);
//            category.ifPresent(product::setCategory);
            if (category.isPresent())
                product.setCategory(category.get());
            else {
                responseRest.setMetadata("Respuesta nok", "-1", "La categoria no se encuentra");
                return new ResponseEntity<>(responseRest, HttpStatus.NOT_FOUND);
            }
            Optional<Product> optionalProduct = productDao.findById(productId);
            if (optionalProduct.isPresent()){
                optionalProduct.get().setQuantity(product.getQuantity());
                optionalProduct.get().setPrice(product.getPrice());
                optionalProduct.get().setName(product.getName());
                optionalProduct.get().setPhoto(product.getPhoto());
                optionalProduct.get().setCategory(product.getCategory());
                productDao.save(optionalProduct.get());

                products.add(optionalProduct.get());
                responseRest.getProductResponse().setProducts(products);
                responseRest.setMetadata("Respuesta ok", "00", "Producto actualizado");
            }
            else {
                responseRest.setMetadata("Respuesta nok", "-1", "No se encuentra el producto");
                return new ResponseEntity<>(responseRest, HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
            e.getStackTrace();
        }
        return new ResponseEntity<>(responseRest, HttpStatus.OK);
    }
}
