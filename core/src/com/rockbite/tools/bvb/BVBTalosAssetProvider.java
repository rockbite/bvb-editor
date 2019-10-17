package com.rockbite.tools.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.runtime.assets.AssetProvider;

import java.io.File;

public class BVBTalosAssetProvider implements AssetProvider {

	ObjectMap<String, TextureRegion> regionMap = new ObjectMap<String, TextureRegion>();

	public String lookupLocation;

	@Override
	public TextureRegion findRegion (String s) {
		TextureRegion region = regionMap.get(s);
		if(region == null) {
			region = new TextureRegion(new Texture(Gdx.files.absolute(lookupLocation + File.separator + s + ".png")));
			regionMap.put(s, region);
		}
		return region;
	}

	public void setParentPath (String path) {
		lookupLocation = path;
	}
}
