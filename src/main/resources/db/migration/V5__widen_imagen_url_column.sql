-- Las URLs de imagenes que expone Facebook Marketplace (CDN fbcdn.net) suelen superar
-- los 500 caracteres por los parametros de firma/expiracion que incluyen. VARCHAR(500)
-- truncaba el INSERT y el scraper descartaba TODOS los anuncios (Data truncation error).
-- TEXT no tiene limite practico y la columna no esta indexada, asi que no hay downside.
ALTER TABLE listings MODIFY COLUMN imagen_url TEXT NULL;
