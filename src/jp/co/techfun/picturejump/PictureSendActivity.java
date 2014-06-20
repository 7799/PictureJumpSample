package jp.co.techfun.picturejump;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.techfun.picturejump.bluetooth.PictureSendJob;
import jp.co.techfun.sendemail.SendEmailActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



// 画像送信画面Activity
public class PictureSendActivity extends Activity {
	// ギャラリー表示用リクエストコード
	public static final int REQUEST_CODE_GALLARY = 0;

	// カメラ画面表示用リクエストコード
	private static final int REQUEST_CODE_CAMERA = 1;

	// デバイス検出画面表示用リクエストコード
	private static final int REQUEST_CODE_DEVICE = 2;

	// 送信デバイス
	private BluetoothDevice device;

	// 送信画像
	private Bitmap picture;

	// 非同期実行用スレッドプール
	private ExecutorService executorService;

	// 進捗ダイアログ
	private ProgressDialog progressDialog;
	
	public static Intent intent_pic;
	InputStream in;
	String file_path;
	// onCreateメソッド(画面初期表示イベント)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 非同期実行用のスレッドプール生成
		executorService = Executors.newCachedThreadPool();

		// レイアウト設定ファイル指定
		setContentView(R.layout.picture_send);

		// 「送信先選択」ボタンにリスナー設定
		ImageButton ibtnSelectDevice = (ImageButton) findViewById(R.id.ibtn_select_device);
		ibtnSelectDevice.setOnClickListener(new OnClickListener() {
			// onClickメソッド(クリックイベント)
			@Override
			public void onClick(View v) {
				// 送信デバイス選択画面呼び出し
				Intent intent = new Intent(PictureSendActivity.this,
						DeviceSelectActivity.class);
				startActivityForResult(intent, REQUEST_CODE_DEVICE);
			}
		});

		// 「ギャラリー」ボタンにリスナー設定
		ImageButton ibtnGallery = (ImageButton) findViewById(R.id.ibtn_gallery);
		ibtnGallery.setOnClickListener(new OnClickListener() {
			// onClickメソッド(クリックイベント)
			@Override
			public void onClick(View v) {
				// インテント生成
				intent_pic = new Intent(Intent.ACTION_GET_CONTENT);
				// インテントタイプを画像で設定
				intent_pic.setType("image/*");
				// ギャラリー起動
				startActivityForResult(Intent.createChooser(intent_pic, AppUtil
						.getString(PictureSendActivity.this,
								R.string.picture_select_title)),
						REQUEST_CODE_GALLARY);
			}
		});

		// 「カメラ」ボタンにリスナー設定
		ImageButton ibtnCamera = (ImageButton) findViewById(R.id.ibtn_camera);
		ibtnCamera.setOnClickListener(new OnClickListener() {
			// onClickメソッド(クリックイベント)
			@Override
			public void onClick(View v) {
				// インテント生成
				Intent intent = new Intent(PictureSendActivity.this,
						CameraActivity.class);
				// カメラ画面起動
				startActivityForResult(intent, REQUEST_CODE_CAMERA);
			}
		});

		// 「回転」ボタンにリスナー設定
		ImageButton ibtnPictureRotate = (ImageButton) findViewById(R.id.ibtn_picture_rotate);
		ibtnPictureRotate.setOnClickListener(new OnClickListener() {
			// onClickメソッド(クリックイベント)
			@Override
			public void onClick(View v) {
				// 画像回転処理
				setPicture(AppUtil.rotateBitmap(picture));
			}
		});

		// 「クリア」ボタンにリスナー設定
		Button btnClear = (Button) findViewById(R.id.btn_clear);
		btnClear.setOnClickListener(new OnClickListener() {
			// onClickメソッド(クリックイベント)
			@Override
			public void onClick(View v) {
				// デバイス、画像表示をクリア
				setDevice(null);
				setPicture(null);
			}
		});

		// 「送信」ボタンにリスナー設定
		Button btnSend = (Button) findViewById(R.id.btn_send);
		btnSend.setOnClickListener(new OnClickListener() {
			// onClickメソッド(クリックイベント)
			@Override
			public void onClick(View v) {
				// 画像送信処理
				send();
			}
		});

		
		// 「mail送信」ボタンにリスナー設定
		Button btnSend_mail = (Button) findViewById(R.id.btn_send_mail);
		btnSend_mail.setOnClickListener(new OnClickListener() {
			// onClickメソッド(クリックイベント)
			@Override
			public void onClick(View v) {
				// 画像送信処理
////				send();
			
					send_mail();  //ここみろ！
					
				// 画像送信画面へ遷移			
//                Intent intent =
//                    new Intent(PictureSendActivity.this,
//                        SendEmailActivity.class);
//                startActivity(intent);
//				
			}
			
			@SuppressLint("SimpleDateFormat")
			public void saveBitmapToSd(Bitmap mBitmap) {
				 try {
				 // sdcardフォルダを指定
				 File root = Environment.getExternalStorageDirectory();

				 // 日付でファイル名を作成　
				 Date mDate = new Date();
				 SimpleDateFormat fileName = new SimpleDateFormat("yyyyMMdd_HHmmss");

				 // 保存処理開始
				 FileOutputStream fos = null;
//				 fos = new FileOutputStream(new File(root, fileName.format(mDate) + ".png"));
				 fos = new FileOutputStream(new File(root, 123 + ".png"));

				 // jpegで保存
				 mBitmap.compress(CompressFormat.PNG, 100, fos);

				 // 保存処理終了
				 fos.close();
				 } catch (Exception e) {
				 Log.e("Error", "" + e.toString());
				 }
				}

			private void send_mail() {
				
//				if (in==null){
////					setPicture(BitmapFactory.decodeStream(in));
//					saveBitmapToSd(BitmapFactory.decodeStream(in));
//				}
				storePicture();   //ここみろ！
//				File file = new File("file://"+file_path);  
				Uri uri = Uri.parse("file://" + file_path);
				String[] mailto = new String[]{"hayashi7799@gmail.com"};
				Intent intent = new Intent(Intent.ACTION_SEND);		
				//intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.putExtra(Intent.EXTRA_EMAIL, mailto);
				intent.putExtra(Intent.EXTRA_SUBJECT, "ご無沙汰してます"); 
				intent.putExtra(Intent.EXTRA_TEXT, "お世話になります。"); 
				intent.putExtra(Intent.EXTRA_STREAM,uri );
				intent.setType("image/*");

				try {
					startActivity(Intent.createChooser(intent, "Choose Email Client"));
				} catch (android.content.ActivityNotFoundException ex) {

				}
				
				//これでもいい
//				// TODO Auto-generated method stub
//				Intent intent=new Intent(Intent.ACTION_SEND);
//				String[] recipients = new String[]{"hayashi7799@gmail.com", "",};
//                intent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
//				intent.putExtra(Intent.EXTRA_SUBJECT,"タイトル");
//				intent.putExtra(Intent.EXTRA_TEXT,"テキスト");
//				intent.setType("image/png");
//				intent.putExtra(Intent.EXTRA_STREAM,Uri.parse("file://"+"mnt/sdcard/1.jpeg"));
//				startActivity(intent);
			}
		});

		
		
//		Button btnSend_mail = (Button) findViewById(R.id.btn_send_mail);
//		btnSend_mail.setOnClickListener(new OnClickListener() {
//		 
//		    @Override
//		    public void onClick(View v) {
//		        try {
//		        	
//		        	Utils.createCachedFile(GmailAttacherActivity.this,
//		                            "Test.txt", "This is a test");
//		 
//		            Utils.startActivity(Utils.getSendEmailIntent(
//		                            GmailAttacherActivity.this,
//		                            "<YOUR_EMAIL_HERE>@<YOUR_DOMAIN>.com", "Test",
//		                            "See attached", "Test.txt"));
//		        } catch (IOException e) {
//		            e.printStackTrace();
//		        } catch (ActivityNotFoundException e) {
//		            Toast.makeText(GmailAttacherActivity.this,
//		                "Gmail is not available on this device.",
//		                Toast.LENGTH_SHORT).show();
//		        }
//		    }
//		});
		
		//暗黙的インテントでアプリを起動
		Intent intent = getIntent();
		Uri uri = intent.getData();
		ImageView view1 = null;
		if (uri != null) {
			try {
				Bitmap bmp = Media.getBitmap(getContentResolver(), uri);
				if (bmp != null) 
					view1.setImageBitmap(bmp);
			    // 画像送信画面へ遷移
                Intent intent2 =
                    new Intent(PictureSendActivity.this,
                        PictureSendActivity.class);
                startActivity(intent2);
			} catch (FileNotFoundException e) {		
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		// ボタンの使用可能状態を更新
		updateButtonStatus();
	}

	// onDestroyメソッド(画面破棄イベント)
	@Override
	protected void onDestroy() {
		// スレッドプール終了
		executorService.shutdownNow();
		super.onDestroy();
	}

	// sendメソッド(画像送信処理)
	private void send() {

		// 進捗ダイアログを表示
		showProgress();

		// 画像送信処理を別スレッドで実行
		executorService.execute(new PictureSendJob(device, picture) {

			// handleSendStartedメソッド(画像送信開始イベント)
			@Override
			protected void handleSendStarted(BluetoothDevice device) {
				// UIスレッド実行
				runOnUiThread(new Runnable() {
					// runメソッド(実行処理)
					@Override
					public void run() {
						// 送信開始時に進捗メッセージ変更
						updateProgressMessage(AppUtil.getString(
								PictureSendActivity.this,
								R.string.picture_send_progress_sending));
					}
				});
			}

			// handleSendFinishメソッド(画像送信終了イベント)
			@Override
			protected void handleSendFinish() {
				// UIスレッド実行
				runOnUiThread(new Runnable() {
					// runメソッド(実行処理)
					@Override
					public void run() {
						// 進捗ダイアログ非表示
						hideProgress();
						// メッセージ表示
						AppUtil.showToast(PictureSendActivity.this,
								R.string.picture_send_succeed);
					}
				});
			}

			// handleProgressメソッド(画像送信進捗変更イベント)
			@Override
			protected void handleProgress(final int total, final int progress) {
				// UIスレッド実行
				runOnUiThread(new Runnable() {
					// runメソッド(実行処理)
					@Override
					public void run() {
						// 進捗表示切り替え
						int value = Math.round(progress * 100 / total);
						updateProgressValue(value);
					}
				});
			}

			// handleExceptionメソッド(画像送信例外発生イベント)
			@Override
			protected void handleException(IOException e) {
				// UIスレッド実行
				runOnUiThread(new Runnable() {
					// runメソッド(実行処理)
					@Override
					public void run() {
						// 進捗ダイアログ非表示
						hideProgress();
						// エラーメッセージ表示
						AppUtil.showToast(PictureSendActivity.this,
								R.string.picture_send_failed);
					}
				});

			}
		});
	}

	// onActivityResultメソッド(画面再表示時イベント)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 処理結果がOKの場合、処理終了
		if (resultCode != RESULT_OK) {
			return;
		}

		// デバイス検出の結果の場合
		if (requestCode == REQUEST_CODE_DEVICE) {
			// 検出したデバイス取得
			device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			setDevice(device);

			// ギャラリー画像取得結果の場合
		} else if (requestCode == REQUEST_CODE_GALLARY) {

			try {
				// 選択画像のURIから画像ストリーム取得
				in = getContentResolver().openInputStream(
						data.getData());
				// 画像表示
				setPicture(BitmapFactory.decodeStream(in));
			} catch (FileNotFoundException e) {
				Log.e(getClass().getSimpleName(), "get image failed.", e);
			}

			// カメラ画像取得結果の場合
		} else if (requestCode == REQUEST_CODE_CAMERA) {
			// 一時保存画像表示
			setPicture(BitmapFactory.decodeFile(AppUtil.getPictureTempPath()));
			// 一時保存画像削除
			AppUtil.deletePicture();
		}
	}

	// setDeviceメソッド(デバイス表示処理)
	private void setDevice(BluetoothDevice device) {
		this.device = device;
		TextView textView = (TextView) findViewById(R.id.tv_selected_device);
		textView.setText(device != null ? device.getName() : "");

		updateButtonStatus();
	}

	// setPictureメソッド(画像表示処理)
	private void setPicture(Bitmap picture) {
		this.picture = picture;
		ImageView imageView = (ImageView) findViewById(R.id.iv_selected_picture);
		imageView.setImageBitmap(AppUtil.resizePicture(picture, 180, 180));

		updateButtonStatus();
	}

	// updateButtonStatusメソッド(ボタンの使用可能状態更新処理)
	private void updateButtonStatus() {

		View send = findViewById(R.id.btn_send);
		send.setEnabled(picture != null && device != null);

		View rotate = findViewById(R.id.ibtn_picture_rotate);
		rotate.setEnabled(picture != null);
		
		View sendEmail = findViewById(R.id.btn_send_mail);
		sendEmail.setEnabled(picture != null );
	}

	// showProgressメソッド(進捗ダイアログ表示処理)
	private void showProgress() {
		// 進捗ダイアログ生成
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(AppUtil.getString(this,
				R.string.picture_send_progress_title));
		progressDialog.setMessage(AppUtil.getString(this,
				R.string.picture_send_progress_waiting));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(100);
		progressDialog.setCancelable(false);
		// ダイアログ表示
		progressDialog.show();
	}

	// hideProgressメソッド(進捗ダイアログ非表示処理)
	private void hideProgress() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	// updateProgressMessageメソッド(進捗メッセージ更新処理)
	private void updateProgressMessage(String message) {
		if (progressDialog != null) {
			progressDialog.setMessage(message);
		}
	}

	// updateProgressValueメソッド(進捗値を更新処理)
	private void updateProgressValue(int value) {
		if (progressDialog != null) {
			progressDialog.setProgress(value);
		}
	}
	
	
	// メールに添付して送信処理
	private void mailto() throws IOException {
		// 画像をSDカードへ保存
		String imgPath = savePicture();
		// メール送信処理インテント
		Intent intent = new Intent();
		
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("image/png");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imgPath));
		startActivity(intent);
		
	}
	
	private String savePicture() throws IOException{
		// SDカードのルートディレクトリ取得
		File dir = Environment.getExternalStorageDirectory();
		File baseDir = new File(dir, "picture_send");
		baseDir.mkdirs();
		// 画像パス(日付形式)
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String imgPath = baseDir + File.separator + format.format(cal.getTime()) + ".png";
		//画像をバイト型に変換
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		picture.compress(CompressFormat.PNG, 100, os);
		os.flush();
		byte[] w = os.toByteArray();
		os.close();
		// 画像を一時的にSDカードに保存
		FileOutputStream out = new FileOutputStream(imgPath);
		out.write(w,0,w.length);
		out.flush();
		
		return imgPath;
	}
	
	
	// storePictureメソッド(画像保存処理)//ここみろ！
    private void storePicture() {

        if (picture != null) {

            try {

                // SDカードのルートディレクトリ取得
                File dir = Environment.getExternalStorageDirectory();
                File baseDir = new File(dir, "picture_jump");
                baseDir.mkdirs();

                // 日付形式のファイル名で画像をSDカードに保存
                SimpleDateFormat format =
                    new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");

                FileOutputStream out =
                    new FileOutputStream(new File(baseDir, format
                        .format(new Date())
                        + ".png"));
//                new FileOutputStream(new File(baseDir, 1234
//                        + ".png"));
                File ts = new File(baseDir, format
                        .format(new Date())
                        + ".png");
                
                file_path = ts.getPath();
                Log.i("PATH",ts.getPath());
                picture.compress(CompressFormat.PNG, 100, out);

                // メッセージ表示
                AppUtil.showToast(this, AppUtil.getString(this,
                    R.string.picture_save_succeed));

            } catch (FileNotFoundException e) {
                Log.e(getClass().getSimpleName(), "picture store failed.", e);
                AppUtil.showToast(this, AppUtil.getString(this,
                    R.string.picture_save_failed));
            }
        }

    }
}
