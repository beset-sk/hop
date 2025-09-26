package sk.b3t.apache.hop.pipeline.transforms.rapidstart;

import org.apache.hop.core.Const;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RapidstartTableDialog extends BaseTransformDialog implements ITransformDialog {

  // Resource bundle marker pre BaseMessages
  private static final Class<?> PKG = RapidstartTableDialog.class;

  private final RapidstartMeta input;
  private Text wPackage;
  private Text wCode;

  public RapidstartTableDialog(
      Shell parent,
      IVariables variables,
      Object meta,
      PipelineMeta pipelineMeta,
      String transformName) {
    super(parent, variables, (ITransformMeta) meta, pipelineMeta, transformName);
    this.input = (RapidstartMeta) meta;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    PropsUi.setLook(shell);
    shell.setText(BaseMessages.getString(PKG, "RapidstartDialog.Shell.Title"));

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);

    int margin = PropsUi.getMargin();

    // Transform name
    Label wlName = new Label(shell, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "RapidstartDialog.TransformName.Label"));
    PropsUi.setLook(wlName);
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 0);
    fdlName.top = new FormAttachment(0, margin);
    fdlName.right = new FormAttachment(25, -margin);
    wlName.setLayoutData(fdlName);

    wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wTransformName);
    wTransformName.setText(Const.NVL(transformName, ""));
    FormData fdName = new FormData();
    fdName.left = new FormAttachment(25, 0);
    fdName.top = new FormAttachment(0, margin);
    fdName.right = new FormAttachment(100, 0);
    wTransformName.setLayoutData(fdName);

    // Package
    Label wlPackage = new Label(shell, SWT.RIGHT);
    wlPackage.setText(BaseMessages.getString(PKG, "RapidstartDialog.Package.Label"));
    PropsUi.setLook(wlPackage);
    FormData fdlPackage = new FormData();
    fdlPackage.left = new FormAttachment(0, 0);
    fdlPackage.top = new FormAttachment(wTransformName, margin * 2);
    fdlPackage.right = new FormAttachment(25, -margin);
    wlPackage.setLayoutData(fdlPackage);

    wPackage = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wPackage);
    FormData fdPackage = new FormData();
    fdPackage.left = new FormAttachment(25, 0);
    fdPackage.top = new FormAttachment(wTransformName, margin * 2);
    fdPackage.right = new FormAttachment(100, 0);
    wPackage.setLayoutData(fdPackage);

    // Code
    Label wlCode = new Label(shell, SWT.RIGHT);
    wlCode.setText(BaseMessages.getString(PKG, "RapidstartDialog.Code.Label"));
    PropsUi.setLook(wlCode);
    FormData fdlCode = new FormData();
    fdlCode.left = new FormAttachment(0, 0);
    fdlCode.top = new FormAttachment(wPackage, margin * 2);
    fdlCode.right = new FormAttachment(25, -margin);
    wlCode.setLayoutData(fdlCode);

    wCode = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wCode);
    FormData fdCode = new FormData();
    fdCode.left = new FormAttachment(25, 0);
    fdCode.top = new FormAttachment(wPackage, margin * 2);
    fdCode.right = new FormAttachment(100, 0);
    wCode.setLayoutData(fdCode);

    Label wlPrevTr = new Label(shell, SWT.RIGHT);
    wlPrevTr.setText("Prev");
    PropsUi.setLook(wlCode);
    FormData fdlPrevTr = new FormData();
    fdlPrevTr.left = new FormAttachment(0, 0);
    fdlPrevTr.top = new FormAttachment(wCode, margin * 2);
    fdlPrevTr.right = new FormAttachment(25, -margin);
    wlPrevTr.setLayoutData(fdlPrevTr);

    String[] prevTransformNames = this.pipelineMeta.getPrevTransformNames(transformName);

    System.out.println("===============" + prevTransformNames.length);
    Text cache = wCode;
    for (String trName : prevTransformNames) {
      System.out.println(trName);
      Text wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
      PropsUi.setLook(wName);
      FormData fdTr = new FormData();
      fdTr.left = new FormAttachment(25, 0);
      fdTr.top = new FormAttachment(cache, margin * 2);
      fdTr.right = new FormAttachment(100, 0);
      wName.setLayoutData(fdTr);
      wName.setText(trName);
      cache = wName;
    }

    // Tlačidlá OK/Cancel
    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "RapidstartDialog.Ok"));
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "RapidstartDialog.Cancel"));
    setButtonPositions(new Button[] {wOk, wCancel}, margin, null);

    // Listeners
    wOk.addListener(SWT.Selection, e -> ok());
    wCancel.addListener(SWT.Selection, e -> cancel());

    // Naplň aktuálne hodnoty
    getData();

    shell.setSize(520, 260);
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) display.sleep();
    }
    return transformName;
  }

  private void getData() {
    wPackage.setText(Const.NVL(input.getPackageName(), ""));
    wCode.setText(Const.NVL(input.getCode(), ""));
    wTransformName.selectAll();
  }

  private void ok() {
    transformName = wTransformName.getText();
    input.setPackageName(wPackage.getText());
    input.setCode(wCode.getText());
    dispose();
  }

  private void cancel() {
    transformName = null;
    dispose();
  }
}
