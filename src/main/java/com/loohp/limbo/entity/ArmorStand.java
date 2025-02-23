/*
 * This file is part of Limbo.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loohp.limbo.entity;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.entity.DataWatcher.WatchableField;
import com.loohp.limbo.entity.DataWatcher.WatchableObjectType;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.world.World;
import org.cloudburstmc.math.vector.Vector3f;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;

import java.util.UUID;

public class ArmorStand extends LivingEntity {
	
	@WatchableField(MetadataIndex = 15, WatchableObjectType = WatchableObjectType.BYTE, IsBitmask = true, Bitmask = 0x01) 
	protected boolean small = false;
	@WatchableField(MetadataIndex = 15, WatchableObjectType = WatchableObjectType.BYTE, IsBitmask = true, Bitmask = 0x04) 
	protected boolean arms = false;
	@WatchableField(MetadataIndex = 15, WatchableObjectType = WatchableObjectType.BYTE, IsBitmask = true, Bitmask = 0x08) 
	protected boolean noBasePlate = false;
	@WatchableField(MetadataIndex = 15, WatchableObjectType = WatchableObjectType.BYTE, IsBitmask = true, Bitmask = 0x10) 
	protected boolean marker = false;
	@WatchableField(MetadataIndex = 16, WatchableObjectType = WatchableObjectType.ROTATION) 
	protected Vector3f headRotation = Vector3f.from(0.0, 0.0, 0.0);
	@WatchableField(MetadataIndex = 17, WatchableObjectType = WatchableObjectType.ROTATION) 
	protected Vector3f bodyRotation = Vector3f.from(0.0, 0.0, 0.0);
	@WatchableField(MetadataIndex = 18, WatchableObjectType = WatchableObjectType.ROTATION) 
	protected Vector3f leftArmRotation = Vector3f.from(-10.0, 0.0, -10.0);
	@WatchableField(MetadataIndex = 19, WatchableObjectType = WatchableObjectType.ROTATION) 
	protected Vector3f rightArmRotation = Vector3f.from(-15.0, 0.0, 10.0);
	@WatchableField(MetadataIndex = 20, WatchableObjectType = WatchableObjectType.ROTATION) 
	protected Vector3f leftLegRotation = Vector3f.from(-1.0, 0.0, -1.0);
	@WatchableField(MetadataIndex = 21, WatchableObjectType = WatchableObjectType.ROTATION) 
	protected Vector3f rightLegRotation = Vector3f.from(1.0, 0.0, 1.0);
	
	public ArmorStand(int entityId, UUID uuid, World world, double x, double y, double z, float yaw, float pitch) {
		super(EntityType.ARMOR_STAND, entityId, uuid, world, x, y, z, yaw, pitch);
	}
	
	public ArmorStand(UUID uuid, World world, double x, double y, double z, float yaw, float pitch) {
		this(Limbo.getInstance().getNextEntityId(), uuid, world, x, y, z, yaw, pitch);
	}
	
	public ArmorStand(World world, double x, double y, double z, float yaw, float pitch) { 
		this(Limbo.getInstance().getNextEntityId(), UUID.randomUUID(), world, x, y, z, yaw, pitch);
	}
	
	public ArmorStand(UUID uuid, Location location) { 
		this(Limbo.getInstance().getNextEntityId(), uuid, location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	
	public ArmorStand(Location location) { 
		this(Limbo.getInstance().getNextEntityId(), UUID.randomUUID(), location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	public boolean isSmall() {
		return small;
	}

	public void setSmall(boolean small) {
		this.small = small;
	}

	public boolean showArms() {
		return arms;
	}

	public void setArms(boolean arms) {
		this.arms = arms;
	}

	public boolean hasNoBasePlate() {
		return noBasePlate;
	}

	public void setNoBasePlate(boolean noBasePlate) {
		this.noBasePlate = noBasePlate;
	}

	public boolean isMarker() {
		return marker;
	}

	public void setMarker(boolean marker) {
		this.marker = marker;
	}

	public Vector3f getHeadRotation() {
		return headRotation;
	}

	public void setHeadRotation(Vector3f headRotation) {
		this.headRotation = headRotation;
	}

	public Vector3f getBodyRotation() {
		return bodyRotation;
	}

	public void setBodyRotation(Vector3f bodyRotation) {
		this.bodyRotation = bodyRotation;
	}

	public Vector3f getLeftArmRotation() {
		return leftArmRotation;
	}

	public void setLeftArmRotation(Vector3f leftArmRotation) {
		this.leftArmRotation = leftArmRotation;
	}

	public Vector3f getRightArmRotation() {
		return rightArmRotation;
	}

	public void setRightArmRotation(Vector3f rightArmRotation) {
		this.rightArmRotation = rightArmRotation;
	}

	public Vector3f getLeftLegRotation() {
		return leftLegRotation;
	}

	public void setLeftLegRotation(Vector3f leftLegRotation) {
		this.leftLegRotation = leftLegRotation;
	}

	public Vector3f getRightLegRotation() {
		return rightLegRotation;
	}

	public void setRightLegRotation(Vector3f rightLegRotation) {
		this.rightLegRotation = rightLegRotation;
	}
}
