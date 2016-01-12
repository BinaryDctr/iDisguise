package de.robingrether.idisguise.disguise;

import java.util.Locale;

/**
 * Represents a disguise as a creeper.
 * 
 * @since 4.0.1
 * @author RobinGrether
 */
public class CreeperDisguise extends MobDisguise {
	
	private static final long serialVersionUID = -5787233589068911050L;
	private boolean powered;
	
	/**
	 * Creates an instance.
	 * 
	 * @since 4.0.1
	 */
	public CreeperDisguise() {
		this(false);
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @since 4.0.1
	 * @param powered whether the creeper should be powered
	 */
	public CreeperDisguise(boolean powered) {
		super(DisguiseType.CREEPER);
		this.powered = powered;
	}
	
	/**
	 * Indicates whether the creeper is powered.
	 * 
	 * @since 4.0.1
	 * @return <code>true</code>, if the creeper is powered
	 */
	public boolean isPowered() {
		return powered;
	}
	
	/**
	 * Sets whether the creeper is powered.
	 * 
	 * @since 4.0.1
	 * @param powered <code>true</code>, if the creeper should be powered
	 */
	public void setPowered(boolean powered) {
		this.powered = powered;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public CreeperDisguise clone() {
		CreeperDisguise clone = new CreeperDisguise(powered);
		clone.setCustomName(customName);
		return clone;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object object) {
		return super.equals(object) && object instanceof CreeperDisguise && ((CreeperDisguise)object).powered == powered;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean applySubtype(String argument) {
		if(super.applySubtype(argument)) {
			return true;
		} else {
			switch(argument.replace('-', '_').toLowerCase(Locale.ENGLISH)) {
				case "powered":
				case "charged":
					setPowered(true);
					return true;
				case "normal":
				case "not_powered":
				case "notpowered":
				case "not_charged":
				case "notcharged":
					setPowered(false);
					return true;
				default:
					return false;
			}
		}
	}
	
}