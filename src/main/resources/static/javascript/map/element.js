/**
 * 
 */

const preloadImage = (src, callback) => {
	const img = new Image();
	img.src = src;
	img.onload = () => callback(null, src);
	img.onerror = () => callback(new Error('Image load error'));
};

const recordListElement = (index) => {
	const profileImgSrc = `/user/rest/profile/${recordList[index].profileImageName}`;
	const subImgSrc = `/user/rest/profile/sub.png`;
	const categoryImgSrc = recordList[index].categoryImoji != null ? `/user/rest/emoji/${recordList[index].categoryImoji}` : 'https://via.placeholder.com/150?text=No+Image';
	const recordImgSrc = recordList[index].imageName && recordList[index].imageName.length > 0 ? 
		`/user/rest/thumbnail/${recordList[index].imageName[0].imageTagText}` : 
		'https://via.placeholder.com/150?text=No+Image';
	const htmlString = `
		<div class="card border-0 border-bottom mb-3 container" onClick='clickMarkerFromCard(${JSON.stringify(index)}, recordList)'>
		
			<div class="profileImageContainer">
				<img id="profileImg-${index}" class="rounded-image" src="https://via.placeholder.com/150?text=Loading..." />
	        	<span class="fs-5 ">${recordList[index].nickName}</span>
	        	${recordList[index].subscribed ? `<img src="https://via.placeholder.com/150?text=Loading..." class="rounded-image subImage"/>` : ''}
			</div><!-- 프로필 상자 -->

	    	<div class="row g-0">
	      		<div class="col-9 card-body p-1">
	            	<div class="mb-4">
	                	<h5 class="card-title fw-bold ellipsis fs-3">${index+1}. ${recordList[index].recordTitle}</h5>
	            	</div><!-- 제목 -->
	            
	      			<div>    
	      				<img id="category-img-${index}" src="https://via.placeholder.com/150?text=Loading..." alt="Category Imoji"
	    				class="recordEmoji mr-3" data-categoryNo ="${recordList[index].categoryNo}"/>
	    		
	    				<span class="badge bg-primary">${recordList[index].stringDistance}</span>
	      			</div><!-- 이모지 + 거리 -->
	      			
	          		<p class="card-text">
        				${recordList[index].hashtag && recordList[index].hashtag.length > 0 ? recordList[index].hashtag.map(tag => `
         				<a href="#"> <small class="text-primary hashTag">${tag.imageTagText}</small></a>`).join('') : ''}
    				</p><!-- 해시태그 -->
	      	
	          		<p class="card-text text-muted mt-3"><i class="fas fa-calendar"></i> ${recordList[index].recordAddDate}</p><!-- 날짜 -->
	          		
	      		</div><!-- 본문 중간부분 col-9 -->
	      
	      	<div class="col-3">
	      		<div class="recordImageContainer">
	      			<img id="record-img-${index}" src="https://via.placeholder.com/150?text=Loading..." class="img-fluid rounded-start" alt="기록 사진"/>
	      		</div>
	      	</div><!-- 사진 부분 col-3 -->
	     </div><!--row-->
	  </div><!-- card -->
	`;

	const htmlElement = $(htmlString);
	
	preloadImage(categoryImgSrc, (err, src) => {
		const categoryImgElement = htmlElement.find(`#category-img-${index}`);
		categoryImgElement.attr('src', err ? categoryImgSrc : src);
  	});

	preloadImage(recordImgSrc, (err, src) => {
    	const recordImgElement = htmlElement.find(`#record-img-${index}`);
    	recordImgElement.attr('src', err ? recordImgSrc : src);
  	});
  		
  	preloadImage(subImgSrc, (err, src) => {
    	const subImgElement = htmlElement.find(`.subImage`);
    	subImgElement.attr('src', err ? subImgSrc : src);
  	});
  	
  	preloadImage(profileImgSrc, (err, src) => {
    	const profileImgElement = htmlElement.find(`#profileImg-${index}`);
    	profileImgElement.attr('src', err ? profileImgSrc : src);
  	});
  
  return htmlElement;
};

const simpleRecordElement = (index) => {
	const categoryImgSrc = recordList[index].categoryImoji != null ? `/user/rest/emoji/${recordList[index].categoryImoji}` : 'https://via.placeholder.com/150?text=No+Image';
	const recordImgSrc = recordList[index].imageName && recordList[index].imageName.length > 0 ? 
		`/user/rest/thumbnail/${recordList[index].imageName[0].imageTagText}` : 
		'https://via.placeholder.com/150?text=No+Image';
	const profileImgSrc = `/user/rest/profile/${recordList[index].profileImageName}`;
	const subImgSrc = `/user/rest/profile/sub.png`;
	const htmlString = `
	        <div class="card border-0 border-bottom mb-3 simpleRecord container" data-index=${index} >
	        
	        	<div class="row">
	        		<div class="profileImageContainer col-6">
						<img id="profileImg-${index}" class="rounded-image" src="/user/rest/profile/${recordList[index].profileImageName}" />
	        			<span class="fs-5 ">${recordList[index].nickName}</span>
	        			${recordList[index].subscribed ? `<img src="/user/rest/profile/sub.png" class="rounded-image subImage"/>` : ''}
					</div><!-- 프로필 상자 -->
					
					<div class="routeButtonGroup col-6">
	        			<button id="routeButton" class="btn btn-primary"><i class="fas fa-directions"></i></button>
	        			
    					<div class="routeAdditionalButtons">
      						<button class="btn btn-primary pedestrianRouteButton routeButton"><i class="fas fa-walking"></i></button>
	        				<button class="btn btn-primary carRouteButton routeButton"><i class="fas fa-car"></i></button>
	        				<button class="btn btn-primary transitRouteButton routeButton"><i class="fas fa-bus"></i></button>
	        			</div>
	        		</div>
	        	</div><!-- row -->
	        		
				<div class="row g-0">
	      			<div class="col-9 card-body p-1">
	            		<div class="mb-4 border-bottom">
	                		<h5 class="card-title fw-bold ellipsis fs-3">${index+1}. ${recordList[index].recordTitle}</h5>
	            		</div><!-- 제목 -->
	            		
	      			<div class="border-0 border-bottom">    
	      				<img src="/user/rest/emoji/${recordList[index].categoryImoji}" alt="Category Imoji"
	    				class="recordEmoji mr-3" data-categoryNo ="${recordList[index].categoryNo}"/>
	    		
	    				<span class="badge bg-primary">${recordList[index].stringDistance}</span>
	    				<span class="badge bg-primary">${recordList[index].markerTypeString}</span>
	      			</div><!-- 이모지 + 거리 -->
	      			
	      			<p class="card-text">
        				${recordList[index].hashtag && recordList[index].hashtag.length > 0 ? recordList[index].hashtag.map(tag => `
         				<a href="#"> <small class="text-primary hashTag">${tag.imageTagText}</small></a>`).join('') : ''}
    				</p><!-- 해시태그 -->
	      			
	      			<div class="mt-3">
	      				<p class="card-text text-muted border-bottom"><i class="fas fa-calendar"></i> ${recordList[index].recordAddDate}</p><!-- 날짜 -->
	      			</div>
	          		
	      			</div><!-- 본문 중간부분 col-9 -->
	      
	      		<div class="col-3">
	      			<div class="recordImageContainer">
	      				<img id="record-img-${index}" src="https://via.placeholder.com/150?text=Loading..." class="img-fluid rounded-start" alt="기록 사진"/>
	      			</div>
	      		</div><!-- 사진 부분 col-3 -->
	     	</div><!--row-->
	     	
	    	<div class="mt-3">
	    		<p class="card-text fs-5 border-0"><i class="fas fa-map-marker-alt"></i> ${recordList[index].checkpointAddress}</p><!-- 주소 -->
	        </div>
	    `;
	    
	    const htmlElement = $(htmlString);
  
  		preloadImage(categoryImgSrc, (err, src) => {
    		const categoryImgElement = htmlElement.find(`.recordEmoji`);
    		categoryImgElement.attr('src', err ? categoryImgSrc : src);
  		});

  		preloadImage(recordImgSrc, (err, src) => {
    		const recordImgElement = htmlElement.find(`#record-img-${index}`);
    		recordImgElement.attr('src', err ? recordImgSrc : src);
  		});
  		
  		preloadImage(subImgSrc, (err, src) => {
    		const subImgElement = htmlElement.find(`.subImage`);
    		subImgElement.attr('src', err ? subImgSrc : src);
  		});
  		
  		preloadImage(profileImgSrc, (err, src) => {
    		const profileImgElement = htmlElement.find(`#profileImg-${index}`);
    		profileImgElement.attr('src', err ? profileImgSrc : src);
  		});
  
  return htmlElement;
}

const detailRecordElement = (index) => {
	const categoryImgSrc = recordList[index].categoryImoji != null ? `/user/rest/emoji/${recordList[index].categoryImoji}` : 'https://via.placeholder.com/150?text=No+Image';
	const profileImgSrc = `/user/rest/profile/${recordList[index].profileImageName}`;
	const subImgSrc = `/user/rest/profile/sub.png`;
	const htmlString = `
	        <div class="card border-0 border-bottom mb-3 detailRecord container" data-index=${index} >
	        
	        	<div class="row">
	        		<div class="profileImageContainer col-6">
						<img id="profileImg-${index}" class="rounded-image" src="/user/rest/profile/${recordList[index].profileImageName}" />
	        			<span class="fs-5 ">${recordList[index].nickName}</span>
	        			${recordList[index].subscribed ? `<img src="/user/rest/profile/sub.png" class="rounded-image subImage"/>` : ''}
					</div><!-- 프로필 상자 -->
					
					<div class="routeButtonGroup col-6">
	        			<button id="routeButton" class="btn btn-primary"><i class="fas fa-directions"></i></button>
	        			
    					<div class="routeAdditionalButtons">
      						<button class="btn btn-primary pedestrianRouteButton routeButton"><i class="fas fa-walking"></i></button>
	        				<button class="btn btn-primary carRouteButton routeButton"><i class="fas fa-car"></i></button>
	        				<button class="btn btn-primary transitRouteButton routeButton"><i class="fas fa-bus"></i></button>
	        			</div>
	        		</div>
	        	</div><!-- row -->
	        		
				<div class="row g-0">
	      			<div class="col-9 card-body p-1">
	            		<div class="mb-4 border-bottom">
	                		<h5 class="card-title fw-bold ellipsis fs-3">${index+1}. ${recordList[index].recordTitle}</h5>
	            		</div><!-- 제목 -->
	            		
	      			<div class="border-0 border-bottom">    
	      				<img src="/user/rest/emoji/${recordList[index].categoryImoji}" alt="Category Imoji"
	    				class="recordEmoji mr-3" data-categoryNo ="${recordList[index].categoryNo}"/>
	    		
	    				<span class="badge bg-primary">${recordList[index].stringDistance}</span>
	    				<span class="badge bg-primary">${recordList[index].markerTypeString}</span>
	      			</div><!-- 이모지 + 거리 -->
	      			
	      			<p class="card-text">
        				${recordList[index].hashtag && recordList[index].hashtag.length > 0 ? recordList[index].hashtag.map(tag => `
         				<a href="#"> <small class="text-primary hashTag">${tag.imageTagText}</small></a>`).join('') : ''}
    				</p><!-- 해시태그 -->
	      			
	      			<div class="mt-3">
	      				<p class="card-text text-muted border-bottom"><i class="fas fa-calendar"></i> ${recordList[index].recordAddDate}</p><!-- 날짜 -->
	      			</div>
	          		
	          		<div class="mt-3">
	          			<p class="card-text fs-5 border-0"><i class="fas fa-map-marker-alt"></i> ${recordList[index].checkpointAddress}</p><!-- 주소 -->
	          		</div>
	          		
	      			</div><!-- 본문 중간부분 col-9 -->
	      		
	      		<div class="media container mt-3">
	      			<video width="100%" height="100%" controls>
                		<source src="/timeline/rest/media/${recordList[index].mediaName}" type="video/mp4">
                	</video>
                </div>
                
                ${recordList[index].imageName && recordList[index].imageName.length > 0 ?
                `
                <div class="swiper mySwiper">
                	<div class="swiper-wrapper">
                		${recordList[index].imageName.map(image => 
                		` <div class="swiper-slide">
                    		<img src="/user/rest/thumbnail/${image.imageTagText}" class="swiperImg"/>
                    	   </div>
                    `).join('')}
                	</div>
                	
                	<div class="swiper-pagination"></div>
                	<div class="swiper-scrollbar"></div>
                </div>
                `
                :'' }
        		
    			<p class="card-text mt-3">${recordList[index].recordText}</p><!-- 기록 텍스트 -->
	    `;
	    
	    const htmlElement = $(htmlString);
	    
	    preloadImage(categoryImgSrc, (err, src) => {
    		const categoryImgElement = htmlElement.find(`.recordEmoji`);
    		categoryImgElement.attr('src', err ? categoryImgSrc : src);
  		});
  		
  		recordList[index].imageName.map( (image, index) =>
  			preloadImage("/user/rest/thumbnail/"+ image.imageTagText, (err, src) => {
    			const recordImgElement = htmlElement.find(`.swiperImg`)[index];
    			$(recordImgElement).attr('src', err ? "/user/rest/thumbnail/"+image.imageTagText : src);
  			})
  		);
  		
  		preloadImage(subImgSrc, (err, src) => {
    		const subImgElement = htmlElement.find(`.subImage`);
    		subImgElement.attr('src', err ? subImgSrc : src);
  		});
  		
  		preloadImage(profileImgSrc, (err, src) => {
    		const profileImgElement = htmlElement.find(`#profileImg`);
    		profileImgElement.attr('src', err ? profileImgSrc : src);
  		});
  		
  		setTimeout(() => {
        	new Swiper(htmlElement.find('.mySwiper')[0], {
            	direction: 'horizontal', // 가로 방향 슬라이드
            	loop: true,
            	slidesPerView: 1, // 한 번에 1개 슬라이드만 보이도록 설정
            	spaceBetween: 10,
            
            	pagination: {
                	el: '.swiper-pagination',
                	clickable: true,
            	},
            	scrollbar: {
                	el: '.swiper-scrollbar',
            	},
        	});
    	}, 0);
  
  		return htmlElement;
}

const recommendListElement = (index) => {
	return `<div class="card border-0 border-bottom mb-3 place" onClick='clickMarkerFromCard( ${JSON.stringify(index)}, placeList )'>
    			<h2 class="card-title fw-bold ellipsis fs-3">${index+1}. ${placeList[index].placeName} </h2>
    			<p class="card-text">${placeList[index].categoryName} </p>
    			<p class="card-text fs-5">${placeList[index].addressName} </p>
    		</div>
    		`;
}// recommendListElement: 추천 장소 리스트

const detailPlaceElement = (index) => {
	return `
	        <div class="card border-0 border-bottom mb-3 detailPlace" data-index=${index}>
	            <div class="g-0">
	                    <div class="card-body">
	                        <h5 class="card-title fw-bold fs-3">${placeList[index].placeName}</h5>
	                        <p class="card-text"><i class="fas fa-list"></i> ${placeList[index].categoryName}</p>
	                        <p class="card-text"><i class="fas fa-map-marker-alt"></i> ${placeList[index].addressName}</p>
    						<p class="card-text"><i class="fas fa-phone"></i> ${placeList[index].phone} </p> 
    						<p class="card-text"><a href=${placeList[index].placeUrl}>More Info</a></p>
	                        
	                </div>
	            </div>
	        </div>
	            		
    		<div class="routeButtonGroup">
	        	<button id="routeButton" class="btn btn-primary"><i class="fas fa-directions"></i></button>
    			<div class="routeAdditionalButtons">
      				<button class="btn btn-primary" onclick="drawRoute('1')"><i class="fas fa-walking"></i></button>
	        		<button class="btn btn-primary" onclick="drawRoute('2')"><i class="fas fa-car"></i></button>
	        		<button class="btn btn-primary" onclick="drawTransitRoute()"><i class="fas fa-bus"></i></button>
 				</div>
	        </div>
	    `;
}
	
const routeListElement = (response) => {
	return `
		<span class="badge bg-primary">총 거리: ${(response.totalDistance / 1000 ).toFixed(1)} km</span>
		<span class="badge bg-primary">총 시간: ${(response.totalTime / 60) < 60 ? (response.totalTime / 60).toFixed(0) +'분' : 
  						( (response.totalTime / 60) / 60).toFixed(0) + '시간 ' +  ( (response.totalTime / 60) % 60).toFixed(0) +'분'
  					 }</span>
		<div class="list-group">
			${response.description.map((item, index) =>
  			`<div class="list-group-item route-guide list-group-flush">${index == 0 ? '출발지에서 ' : ''} ${item} </div>`
			).join('')}
		</div>
`;
}

const transitRouteListElement = (paths) => {
	return `
		${paths.map( (path, index)  => 
		`<div class="list-group-item list-group-item-action">
			<ul class="list-group list-group-flush">
  				<li class="transitRoute list-group-item justify-content-between align-items-center" data-index =${index}>
  					<h1>${(path.totalTime / 60) < 60 ? (path.totalTime / 60).toFixed(0) +'분' : 
  						( (path.totalTime / 60) / 60).toFixed(0) + '시간 ' +  ( (path.totalTime / 60) % 60).toFixed(0) +'분'
  					 }</h1>
    				<span>도보 ${(path.totalWalkTime/60).toFixed(0) }분</span>
    				<span>환승 ${path.transferCount}회</span>
    				<span>요금 ${path.totalFare}원</span>
    				<span>거리 ${(path.totalDistance / 1000 ).toFixed(1)}km</span>
  				</li>
			</ul>
		</div>`
		).join('')}
		`;
}

const transitRouteDescriptionElement = (path) => {
	return `
		<div class="list-group list-group-flush">
		${path.routes.map((route) => `
			<div class="transitRoutePath list-group-item">
				${route.mode === 'WALK' ? route.endName + '까지 도보로 이동' : route.startName + '에서 ' + route.routeName + ' 승차 후 ' + route.endName + ' 하차'}
			 </div>
		`).join('')}
		</div>
		`;
}