MERGE INTO transportistas (id, nombre, rut, email_contacto) KEY(id)
VALUES
    (1, 'Transportes Andes', '76000001-1', 'operaciones@transportesandes.cl'),
    (2, 'Logistica Pacifico', '76000002-2', 'contacto@logisticapacifico.cl');

ALTER TABLE transportistas ALTER COLUMN id RESTART WITH 100;
