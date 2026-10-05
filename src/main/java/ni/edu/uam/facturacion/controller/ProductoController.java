package ni.edu.uam.facturacion.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.facturacion.dao.CategoriaDAO;
import ni.edu.uam.facturacion.dao.ProductoDAO;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class ProductoController {

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtExistencia;

    @FXML
    private TextField txtRutaImagen;

    @FXML
    private ImageView imgProducto;

    @FXML
    private ComboBox<Categoria> cmbCategoria;

    @FXML
    private CheckBox chkActivo;

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private ComboBox<Categoria> cmbFiltroCategoria;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    @FXML
    private TableView<Producto> tblProductos;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Categoria> colCategoria;

    @FXML
    private TableColumn<Producto, BigDecimal> colPrecio;

    @FXML
    private TableColumn<Producto, Integer> colExistencia;

    @FXML
    private TableColumn<Producto, Boolean> colActivo;


    private static final Path CARPETA_IMAGENES =
            Path.of(
                    "src",
                    "main",
                    "resources",
                    "ni",
                    "edu",
                    "uam",
                    "facturacion",
                    "images",
                    "productos"
            );


    private final ProductoDAO productoDAO =
            new ProductoDAO();

    private final CategoriaDAO categoriaDAO =
            new CategoriaDAO();


    private final ObservableList<Producto> productos =
            FXCollections.observableArrayList();


    private final FilteredList<Producto> productosFiltrados =
            new FilteredList<>(
                    productos,
                    p -> true
            );


    private final Categoria todasCategorias =
            new Categoria(
                    null,
                    "Todas las categorías",
                    true
            );


    private Producto seleccionado;


    @FXML
    private void initialize() {

        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo")
        );

        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );

        colCategoria.setCellValueFactory(
                new PropertyValueFactory<>("categoria")
        );

        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precioVenta")
        );

        colExistencia.setCellValueFactory(
                new PropertyValueFactory<>("existencia")
        );

        colActivo.setCellValueFactory(
                new PropertyValueFactory<>("activo")
        );


        tblProductos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblProductos.setItems(
                productosFiltrados
        );


        cmbEstado.setItems(
                FXCollections.observableArrayList(
                        "Todos",
                        "Activos",
                        "Inactivos"
                )
        );

        cmbEstado.setValue("Todos");

        cmbFiltroCategoria.setValue(
                todasCategorias
        );


        txtBuscar
                .textProperty()
                .addListener(
                        (obs, anterior, nuevo) ->
                                aplicarFiltros()
                );


        cmbEstado
                .valueProperty()
                .addListener(
                        (obs, anterior, nuevo) ->
                                aplicarFiltros()
                );


        cmbFiltroCategoria
                .valueProperty()
                .addListener(
                        (obs, anterior, nuevo) ->
                                aplicarFiltros()
                );


        tblProductos
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, anterior, actual) ->
                                mostrar(actual)
                );


        chkActivo.setSelected(true);

        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);


        cargarCategorias();
        cargarProductos();
    }


    private void cargarCategorias() {

        try {

            var todas =
                    categoriaDAO.listar();


            cmbCategoria.setItems(
                    FXCollections.observableArrayList(
                            todas
                                    .stream()
                                    .filter(Categoria::isActiva)
                                    .toList()
                    )
            );


            ObservableList<Categoria> filtro =
                    FXCollections.observableArrayList(
                            todasCategorias
                    );


            filtro.addAll(todas);

            cmbFiltroCategoria.setItems(
                    filtro
            );

            cmbFiltroCategoria.setValue(
                    todasCategorias
            );


        } catch (SQLException e) {

            registrarErrorSQL(
                    "cargar las categorías",
                    e
            );

            mensaje(
                    Alert.AlertType.ERROR,
                    "No fue posible cargar las categorías."
            );
        }
    }


    private void cargarProductos() {

        try {

            productos.setAll(
                    productoDAO.listar()
            );

        } catch (SQLException e) {

            registrarErrorSQL(
                    "cargar los productos",
                    e
            );

            mensaje(
                    Alert.AlertType.ERROR,
                    "No fue posible cargar los productos."
            );
        }
    }


    private void aplicarFiltros() {

        String texto =
                txtBuscar.getText() == null
                        ? ""
                        : txtBuscar
                        .getText()
                        .trim()
                        .toLowerCase();


        String estado =
                cmbEstado.getValue();


        Categoria categoria =
                cmbFiltroCategoria.getValue();


        productosFiltrados.setPredicate(
                producto -> {

                    boolean coincideTexto =
                            texto.isEmpty()
                                    || producto
                                    .getCodigo()
                                    .toLowerCase()
                                    .contains(texto)

                                    || producto
                                    .getNombre()
                                    .toLowerCase()
                                    .contains(texto)

                                    || producto
                                    .getCategoria()
                                    .getNombre()
                                    .toLowerCase()
                                    .contains(texto);


                    boolean coincideEstado =
                            estado == null
                                    || estado.equals("Todos")
                                    || (
                                    estado.equals("Activos")
                                            == producto.isActivo()
                            );


                    boolean coincideCategoria =
                            categoria == null
                                    || categoria.getId() == null
                                    || Objects.equals(
                                    categoria.getId(),
                                    producto
                                            .getCategoria()
                                            .getId()
                            );


                    return coincideTexto
                            && coincideEstado
                            && coincideCategoria;
                }
        );
    }


    private void mostrar(
            Producto producto
    ) {

        seleccionado = producto;


        btnEliminar.setDisable(
                producto == null
        );

        btnActualizar.setDisable(
                producto == null
        );

        btnGuardar.setDisable(
                producto != null
        );


        if (producto == null) {
            return;
        }


        txtCodigo.setText(
                producto.getCodigo()
        );

        txtNombre.setText(
                producto.getNombre()
        );

        txtPrecio.setText(
                producto
                        .getPrecioVenta()
                        .toPlainString()
        );

        txtExistencia.setText(
                String.valueOf(
                        producto.getExistencia()
                )
        );

        chkActivo.setSelected(
                producto.isActivo()
        );

        mostrarImagen(
                producto.getRutaImagen()
        );


        cmbCategoria
                .getItems()
                .stream()
                .filter(
                        categoria ->
                                Objects.equals(
                                        categoria.getId(),
                                        producto
                                                .getCategoria()
                                                .getId()
                                )
                )
                .findFirst()
                .ifPresentOrElse(
                        cmbCategoria::setValue,
                        () ->
                                cmbCategoria
                                        .getSelectionModel()
                                        .clearSelection()
                );
    }


    @FXML
    private void guardar() {

        Producto producto =
                leerFormulario(null);


        if (producto == null) {
            return;
        }


        try {

            if (productoDAO.existeCodigo(
                    producto.getCodigo()
            )) {

                mensaje(
                        Alert.AlertType.WARNING,
                        "Ya existe un producto con ese código."
                );

                txtCodigo.requestFocus();

                return;
            }


            productoDAO.guardar(
                    producto
            );


            mensaje(
                    Alert.AlertType.INFORMATION,
                    "Producto registrado correctamente."
            );


            nuevo();
            cargarProductos();


        } catch (SQLException e) {

            registrarErrorSQL(
                    "registrar un producto",
                    e
            );

            mensaje(
                    Alert.AlertType.ERROR,
                    "No fue posible registrar el producto."
            );
        }
    }


    @FXML
    private void actualizar() {

        if (seleccionado == null) {

            mensaje(
                    Alert.AlertType.WARNING,
                    "Debe seleccionar el producto que desea actualizar."
            );

            return;
        }


        Producto producto =
                leerFormulario(
                        seleccionado.getId()
                );


        if (producto == null) {
            return;
        }


        try {

            if (productoDAO.existeCodigoExceptoId(
                    producto.getCodigo(),
                    seleccionado.getId()
            )) {

                mensaje(
                        Alert.AlertType.WARNING,
                        "Ya existe otro producto con ese código."
                );

                txtCodigo.requestFocus();

                return;
            }


            productoDAO.actualizar(
                    producto
            );


            mensaje(
                    Alert.AlertType.INFORMATION,
                    "Producto actualizado correctamente."
            );


            nuevo();
            cargarProductos();


        } catch (SQLException e) {

            registrarErrorSQL(
                    "actualizar un producto",
                    e
            );

            mensaje(
                    Alert.AlertType.ERROR,
                    "No fue posible actualizar el producto."
            );
        }
    }


    private Producto leerFormulario(
            Integer id
    ) {

        String codigo =
                txtCodigo
                        .getText()
                        .trim();


        String nombre =
                txtNombre
                        .getText()
                        .trim();


        if (codigo.isEmpty()) {

            return invalido(
                    "El código es obligatorio."
            );
        }


        if (nombre.isEmpty()) {

            return invalido(
                    "El nombre es obligatorio."
            );
        }


        Categoria categoria =
                cmbCategoria.getValue();


        if (categoria == null) {

            return invalido(
                    "Debe seleccionar una categoría."
            );
        }


        BigDecimal precio;


        try {

            precio =
                    new BigDecimal(
                            txtPrecio
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            return invalido(
                    "El precio debe ser un valor numérico."
            );
        }


        if (precio.signum() <= 0) {

            return invalido(
                    "El precio debe ser mayor que cero."
            );
        }


        int existencia;


        try {

            existencia =
                    Integer.parseInt(
                            txtExistencia
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            return invalido(
                    "La existencia debe ser un número entero."
            );
        }


        if (existencia < 0) {

            return invalido(
                    "La existencia no puede ser negativa."
            );
        }


        String rutaImagen =
                txtRutaImagen
                        .getText()
                        .isBlank()
                        ? null
                        : txtRutaImagen.getText();


        return new Producto(
                id,
                codigo,
                nombre,
                categoria,
                precio,
                existencia,
                rutaImagen,
                chkActivo.isSelected()
        );
    }


    private Producto invalido(
            String texto
    ) {

        mensaje(
                Alert.AlertType.WARNING,
                texto
        );

        return null;
    }


    @FXML
    private void eliminar() {

        if (seleccionado == null) {

            mensaje(
                    Alert.AlertType.WARNING,
                    "Debe seleccionar un producto."
            );

            return;
        }


        Alert confirmar =
                new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "¿Eliminar el producto \""
                                + seleccionado.getNombre()
                                + "\"?",
                        ButtonType.OK,
                        ButtonType.CANCEL
                );


        if (
                confirmar
                        .showAndWait()
                        .orElse(ButtonType.CANCEL)
                        != ButtonType.OK
        ) {
            return;
        }


        try {

            productoDAO.eliminar(
                    seleccionado.getId()
            );


            mensaje(
                    Alert.AlertType.INFORMATION,
                    "Producto eliminado correctamente."
            );


            nuevo();
            cargarProductos();


        } catch (SQLException e) {

            registrarErrorSQL(
                    "eliminar un producto",
                    e
            );

            mensaje(
                    Alert.AlertType.ERROR,
                    "No fue posible eliminar el producto."
            );
        }
    }


    @FXML
    private void nuevo() {

        tblProductos
                .getSelectionModel()
                .clearSelection();


        seleccionado = null;


        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();


        mostrarImagen(null);


        cmbCategoria
                .getSelectionModel()
                .clearSelection();


        chkActivo.setSelected(true);


        btnEliminar.setDisable(true);
        btnActualizar.setDisable(true);
        btnGuardar.setDisable(false);


        txtCodigo.requestFocus();
    }


    @FXML
    private void examinarImagen() {

        FileChooser chooser =
                new FileChooser();


        chooser.setTitle(
                "Seleccionar imagen del producto"
        );


        chooser
                .getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Imágenes",
                                "*.png",
                                "*.jpg",
                                "*.jpeg",
                                "*.gif"
                        )
                );


        File archivo =
                chooser.showOpenDialog(
                        txtCodigo
                                .getScene()
                                .getWindow()
                );


        if (archivo == null) {
            return;
        }


        Image imagen =
                new Image(
                        archivo
                                .toURI()
                                .toString()
                );


        if (imagen.isError()) {

            mensaje(
                    Alert.AlertType.WARNING,
                    "No se puede leer esa imagen. "
                            + "Use un archivo PNG, JPG o GIF válido."
            );

            return;
        }


        try {

            Files.createDirectories(
                    CARPETA_IMAGENES
            );


            Path destino =
                    CARPETA_IMAGENES.resolve(
                            UUID.randomUUID()
                                    + "_"
                                    + archivo.getName()
                    );


            Files.copy(
                    archivo.toPath(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );


            mostrarImagen(
                    destino.toString()
            );


        } catch (IOException e) {

            System.err.println(
                    "Error al copiar la imagen: "
                            + e.getMessage()
            );

            e.printStackTrace();

            mensaje(
                    Alert.AlertType.ERROR,
                    "No fue posible guardar la imagen seleccionada."
            );
        }
    }


    @FXML
    private void quitarImagen() {

        mostrarImagen(null);
    }


    private void mostrarImagen(
            String ruta
    ) {

        txtRutaImagen.setText(
                ruta == null
                        ? ""
                        : ruta
        );


        File archivo =
                ruta == null
                        ? null
                        : new File(ruta);


        imgProducto.setImage(
                archivo != null
                        && archivo.exists()

                        ? new Image(
                        archivo
                                .toURI()
                                .toString()
                )

                        : null
        );
    }


    @FXML
    private void cerrar() {

        Stage stage =
                (Stage) txtCodigo
                        .getScene()
                        .getWindow();

        stage.close();
    }


    private void registrarErrorSQL(
            String operacion,
            SQLException e
    ) {

        System.err.println(
                "Error SQL al "
                        + operacion
                        + ": "
                        + e.getMessage()
        );

        e.printStackTrace();
    }


    private void mensaje(
            Alert.AlertType tipo,
            String texto
    ) {

        new Alert(
                tipo,
                texto,
                ButtonType.OK
        ).showAndWait();
    }
}