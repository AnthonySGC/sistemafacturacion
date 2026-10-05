package ni.edu.uam.facturacion.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.facturacion.dao.CategoriaDAO;
import ni.edu.uam.facturacion.model.Categoria;

import java.sql.SQLException;

public class CategoriaController {

    @FXML
    private TextField txtNombre;

    @FXML
    private CheckBox chkActiva;

    @FXML
    private Button btnEliminar;

    @FXML
    private TableView<Categoria> tblCategorias;

    @FXML
    private TableColumn<Categoria, Integer> colId;

    @FXML
    private TableColumn<Categoria, String> colNombre;

    @FXML
    private TableColumn<Categoria, Boolean> colActiva;


    private final CategoriaDAO categoriaDAO =
            new CategoriaDAO();

    private final ObservableList<Categoria> categorias =
            FXCollections.observableArrayList();

    private Categoria seleccionada;


    @FXML
    private void initialize() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );

        colActiva.setCellValueFactory(
                new PropertyValueFactory<>("activa")
        );

        tblCategorias.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblCategorias.setItems(categorias);

        tblCategorias
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, anterior, actual) ->
                                mostrar(actual)
                );

        chkActiva.setSelected(true);

        btnEliminar.setDisable(true);

        cargarCategorias();
    }


    private void cargarCategorias() {

        try {

            categorias.setAll(
                    categoriaDAO.listar()
            );

        } catch (SQLException e) {

            mensaje(
                    Alert.AlertType.ERROR,
                    "Error de base de datos: "
                            + e.getMessage()
            );
        }
    }


    private void mostrar(Categoria categoria) {

        seleccionada = categoria;

        btnEliminar.setDisable(
                categoria == null
        );

        if (categoria == null) {
            return;
        }

        txtNombre.setText(
                categoria.getNombre()
        );

        chkActiva.setSelected(
                categoria.isActiva()
        );
    }


    @FXML
    private void guardar() {

        String nombre =
                txtNombre.getText().trim();

        if (nombre.isEmpty()) {

            mensaje(
                    Alert.AlertType.WARNING,
                    "El nombre de la categoría es obligatorio."
            );

            txtNombre.requestFocus();

            return;
        }


        try {

            /*
             * INSERT
             */
            if (seleccionada == null) {

                if (categoriaDAO.existeNombre(nombre)) {

                    mensaje(
                            Alert.AlertType.WARNING,
                            "Ya existe una categoría con ese nombre."
                    );

                    txtNombre.requestFocus();

                    return;
                }


                Categoria categoria =
                        new Categoria(
                                null,
                                nombre,
                                chkActiva.isSelected()
                        );


                categoriaDAO.guardar(
                        categoria
                );


                mensaje(
                        Alert.AlertType.INFORMATION,
                        "Categoría registrada correctamente."
                );


                /*
                 * UPDATE
                 */
            } else {

                if (categoriaDAO.existeNombreExceptoId(
                        nombre,
                        seleccionada.getId()
                )) {

                    mensaje(
                            Alert.AlertType.WARNING,
                            "Ya existe otra categoría con ese nombre."
                    );

                    txtNombre.requestFocus();

                    return;
                }


                Categoria categoria =
                        new Categoria(
                                seleccionada.getId(),
                                nombre,
                                chkActiva.isSelected()
                        );


                categoriaDAO.actualizar(
                        categoria
                );


                mensaje(
                        Alert.AlertType.INFORMATION,
                        "Categoría actualizada correctamente."
                );
            }


            nuevo();

            cargarCategorias();


        } catch (SQLException e) {

            mensaje(
                    Alert.AlertType.ERROR,
                    "Error de base de datos: "
                            + e.getMessage()
            );
        }
    }


    @FXML
    private void eliminar() {

        /*
         * No se puede eliminar sin haber
         * seleccionado una categoría.
         */
        if (seleccionada == null) {

            mensaje(
                    Alert.AlertType.WARNING,
                    "Debe seleccionar una categoría."
            );

            return;
        }


        Alert confirmar =
                new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "¿Eliminar la categoría \""
                                + seleccionada.getNombre()
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

            /*
             * Antes del DELETE comprobamos si
             * existen productos relacionados.
             */
            if (categoriaDAO.tieneProductos(
                    seleccionada.getId()
            )) {

                mensaje(
                        Alert.AlertType.WARNING,
                        "No puede eliminar la categoría porque tiene productos asociados."
                );

                return;
            }


            categoriaDAO.eliminar(
                    seleccionada.getId()
            );


            mensaje(
                    Alert.AlertType.INFORMATION,
                    "Categoría eliminada correctamente."
            );


            nuevo();

            cargarCategorias();


        } catch (SQLException e) {

            mensaje(
                    Alert.AlertType.ERROR,
                    "Error de base de datos: "
                            + e.getMessage()
            );
        }
    }


    @FXML
    private void nuevo() {

        tblCategorias
                .getSelectionModel()
                .clearSelection();


        seleccionada = null;


        txtNombre.clear();


        chkActiva.setSelected(true);


        btnEliminar.setDisable(true);


        txtNombre.requestFocus();
    }


    @FXML
    private void cerrar() {

        Stage stage =
                (Stage) txtNombre
                        .getScene()
                        .getWindow();

        stage.close();
    }


    private void mensaje(
            Alert.AlertType tipo,
            String texto
    ) {

        Alert alert =
                new Alert(
                        tipo,
                        texto,
                        ButtonType.OK
                );

        alert.showAndWait();
    }
}