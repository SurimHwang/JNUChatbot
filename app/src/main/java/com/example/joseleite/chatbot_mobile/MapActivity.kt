package com.example.joseleite.chatbot_mobile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.*
import java.security.Permission
import android.os.Build
import android.content.Intent
import android.graphics.Color
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.widget.LocationButtonView

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mNaverMap: NaverMap? = null
    private var mLocationSource: FusedLocationSource? = null
    private val PERMISSION_REQUEST_CODE = 100
    private val PERMISSIONS: Array<String> = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var reply: String = ""
    private var que: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        //네이버 지도 객체 생성
        val fm: FragmentManager = getSupportFragmentManager()
        var mapFragment: MapFragment = fm.findFragmentById(R.id.map) as MapFragment
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance()
            fm.beginTransaction().add(R.id.map, mapFragment).commit()
        }
        //위치를 반환하는 구현체인 FusedLocationSource 생성
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
        mapFragment.getMapAsync(this)

        //돌아가기 버튼 리스너
        val backBtn = findViewById<Button>(R.id.backbtn)
        backBtn?.setOnClickListener {
            Toast.makeText(
                    applicationContext, "돌아가기 버튼 클릭됨.",
                    Toast.LENGTH_SHORT
            )
                    .show()
            finish()
        }
    }

    //네이버지도 기본 설정 및 마커 출력
    override fun onMapReady(@NonNull naverMap: NaverMap) {
        Log.d("MainActivity", "onMapReady")

        //NaverMap객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap

        //배경 지도 기본형으로 설정
        mNaverMap!!.setMapType(NaverMap.MapType.Basic)

        //교통 정보 활성화
        mNaverMap!!.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
        mNaverMap!!.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)

        //실내 정보 활성화
        mNaverMap!!.isIndoorEnabled = true

        //네이버 지도 현재 위치 버튼 활성화
        val uiSettings: UiSettings = mNaverMap!!.getUiSettings()
        uiSettings.setLocationButtonEnabled(true)
        val locationbtn = findViewById<LocationButtonView>(R.id.location)
        locationbtn.setMap(mNaverMap)

        mNaverMap!!.setLocationSource(mLocationSource)

        //전남대 캠퍼스로 포커스 설정
        val cameraPosition = CameraPosition(
                LatLng(35.176228, 126.905856), 15.0
        )
        mNaverMap!!.setCameraPosition(cameraPosition)

        //권한확인. 결과는 onRequestPermissionsResult 콜백 메소드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)

        //MainActivity에서 응답 가져오기
        reply = getIntent().getStringExtra("RESPONSE_KEY")
        que = getIntent().getStringExtra("QUESTION_KEY")


        //복사기 마커 생성
        if (reply.contains("복사기")) {
            //공대 복사기
            if (reply.contains("공대 근처에는")) {
                 make_Marker("공6 1층", 35.1797675801189, 126.90862041497837, naverMap)
                 make_Marker("공7 1층", 35.17827223932976, 126.90934870576018, naverMap)
                 make_Marker("공5 1층", 35.17827946146618, 126.91107802601212, naverMap)
                 make_Marker("사범4 1층", 35.17786504522472, 126.9095938586981, naverMap)
            }
            //농대 복사기
            else if(reply.contains("농대 근처에는")){
                 make_Marker("농2 1층", 35.17652725800159, 126.90159534479939, naverMap)
            }
            //도서관 복사기
            else if(reply.contains("도서관 근처에는")){
                 make_Marker("홍도 1층, 4층", 35.17700893844574, 126.90586306168629, naverMap)
                 make_Marker("백도 2층", 35.17806304300721, 126.90689946100932, naverMap)
            }
            //상가 복사집
            else if(reply.contains("용봉캠퍼스 근처에는")){
                make_Marker("전대 정문", 35.17144666175473, 126.90425039269113, naverMap)
                make_Marker("전대 후문", 35.17501828719943, 126.9131141297168, naverMap)
                make_Marker("상대(인문대) 쪽문", 35.17799246751889, 126.90476135298655, naverMap)
                make_Marker("예대 쪽문", 35.18136275294448, 126.90437165184741, naverMap)
                make_Marker("공대 쪽문", 35.17873248263554, 126.91222351805132, naverMap)
                make_Marker("진리관 뒤편", 35.17739593981557, 126.90264345654447, naverMap)
                make_Marker("BTL", 35.18116034734625, 126.90547616172945, naverMap)
            }
            //상대 근처 복사기
            else if(reply.contains("인문대 근처에는")){
                make_Marker("진리관 3층", 35.177090649183626, 126.90325618204258, naverMap)
                make_Marker("경영1 1층", 35.17658184604899, 126.90411890378077, naverMap)
                make_Marker("인문1 1층", 35.17739282686363, 126.90471484426976, naverMap)
            }
            //생활과학대 복사기
            else if(reply.contains("생활과학대 근처에는")){
                make_Marker("생활과학 1층", 35.17387227535789, 126.9112032474336, naverMap)
            }
            //캠퍼스 전체 복사기
            else{
                make_Marker("공6 1층", 35.1797675801189, 126.90862041497837, naverMap)
                make_Marker("공7 1층", 35.17827223932976, 126.90934870576018, naverMap)
                make_Marker("공5 1층", 35.17827946146618, 126.91107802601212, naverMap)
                make_Marker("사범4 1층", 35.17786504522472, 126.9095938586981, naverMap)
                make_Marker("농2 1층", 35.17652725800159, 126.90159534479939, naverMap)
                make_Marker("홍도 1층, 4층", 35.17700893844574, 126.90586306168629, naverMap)
                make_Marker("백도 2층", 35.17806304300721, 126.90689946100932, naverMap)
                make_Marker("진리관 3층", 35.177090649183626, 126.90325618204258, naverMap)
                make_Marker("경영1 1층", 35.17658184604899, 126.90411890378077, naverMap)
                make_Marker("인문1 1층", 35.17739282686363, 126.90471484426976, naverMap)
                make_Marker("BTL", 35.18116034734625, 126.90547616172945, naverMap)
                make_Marker("생활과학 1층", 35.17387227535789, 126.9112032474336, naverMap)
            }
        }


        // 버스 정류장 마커 생성
        if(reply.contains("버스정류장")){
            //캠퍼스 인근
            if(reply.contains("용봉캠퍼스 인근")){
                make_Marker("용봉한화꿈에그린", 35.18109154695949, 126.90430091327045, naverMap)
                make_Marker("도로교통공단/대신파크", 35.18230940682342, 126.90891642203533, naverMap)
                make_Marker("성산맨션", 35.181267422256184, 126.91153031580735, naverMap)
                make_Marker("전남대공과대학", 35.178828653450736, 126.91194168022544, naverMap)
                make_Marker("전남대동문", 35.17688513108259, 126.91248807470838, naverMap)
                make_Marker("전남대스포츠센터", 35.175023394345004, 126.91248232746405, naverMap)
                make_Marker("북구청", 35.17349481520497, 126.91449390324848, naverMap)
                make_Marker("북구보건소", 35.17302920211987, 126.91048865691161, naverMap)
                make_Marker("전남대", 35.17012058109242, 126.90384144549921, naverMap)
                make_Marker("전대치과병원", 35.17178525974846, 126.90233002022514, naverMap)
                make_Marker("용봉2휴먼시아아파트", 35.1717117322158, 126.89825671828198, naverMap)
                make_Marker("용봉우미아파트", 35.17768749017917, 126.90084131525892, naverMap)
                make_Marker("유창허니문", 35.177451050153756, 126.8989270852914, naverMap)
                make_Marker("유창아파트", 35.17696820044486, 126.89729650182416, naverMap)
                make_Marker("전남대용봉탑", 35.17503387541646, 126.9059606564043, naverMap)
            }
        }

        // 건물 위치 마커 생성
        if(reply.contains("공대 1호관은"))
            make_Marker("공대1호관", 35.17962288789317, 126.91020420869783, naverMap)
        else if(reply.contains("공대 2호관은"))
            make_Marker("공대2호관", 35.17962288789317, 126.91020420869783, naverMap)
        else if(reply.contains("공대 3호관은"))
            make_Marker("공대3호관", 35.17900279429033, 126.91059430719321, naverMap)
        else if(reply.contains("공대 4호관은"))
            make_Marker("공대4호관", 35.178624259602785, 126.90890284984663, naverMap)
        else if(reply.contains("공대 5호관은"))
            make_Marker("공대5호관", 35.17803078306004, 126.91106928632647, naverMap)
        else if(reply.contains("공대 6호관은"))
            make_Marker("공대6호관", 35.17972306281237, 126.90862502475159, naverMap)
        else if(reply.contains("공대 7호관은"))
            make_Marker("공대7호관", 35.17827208129237, 126.90929318127704, naverMap)
        else if(reply.contains("사범대 4호관은"))
            make_Marker("사범대4호관", 35.17786374541136, 126.90959549060405, naverMap)
        else if(reply.contains("공과 대학은")) {
            make_Marker("공대1호관", 35.17962288789317, 126.91020420869783, naverMap)
            make_Marker("공대2호관", 35.17962288789317, 126.91020420869783, naverMap)
            make_Marker("공대3호관", 35.17900279429033, 126.91059430719321, naverMap)
            make_Marker("공대4호관", 35.178624259602785, 126.90890284984663, naverMap)
            make_Marker("공대5호관", 35.17803078306004, 126.91106928632647, naverMap)
            make_Marker("공대6호관", 35.17972306281237, 126.90862502475159, naverMap)
            make_Marker("공대7호관", 35.17827208129237, 126.90929318127704, naverMap)
        }

    }

    //네이버 지도 권한 확인 콜백 메소드
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //request code와 권한획득여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap?.setLocationTrackingMode(LocationTrackingMode.Follow)
            }
        }
    }


    //마커 생성 메소드
    fun make_Marker(str: String, lat: Double, lng: Double, naverMap: NaverMap): Marker {

        val marker = Marker()

        //좌표 설정
        marker.position = LatLng(lat, lng)
        marker.map = naverMap

        //정보창 설정
        val infoWindow = InfoWindow()
        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(applicationContext) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
                return str
            }
        }
        infoWindow.open(marker)

        return marker
    }
}